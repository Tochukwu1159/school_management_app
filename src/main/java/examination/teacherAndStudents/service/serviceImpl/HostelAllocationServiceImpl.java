package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.HostelAllocationRequest;
import examination.teacherAndStudents.dto.HostelAllocationResponse;
import examination.teacherAndStudents.dto.PaymentRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.FeePaymentService;
import examination.teacherAndStudents.service.HostelAllocationService;
import examination.teacherAndStudents.utils.AllocationStatus;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HostelAllocationServiceImpl implements HostelAllocationService {

    private final HostelAllocationRepository hostelAllocationRepository;
    private final HostelRepository hostelRepository;
    private final FeePaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final HostelBedTrackerRepository hostelBedTrackerRepository;
    private final ProfileRepository profileRepository;
    private final FeeRepository feeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public HostelAllocationResponse payHotelAllocation(Long feeId, Long sessionId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User student = userRepository.findByEmailAndRole(email, Roles.STUDENT)
                .orElseThrow(() -> new CustomNotFoundException("Please login as a Student"));

        // Validate academic session
        AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found with ID: " + sessionId));

        // Find student profile
        Profile profile = profileRepository.findByUser(student)
                .orElseThrow(() -> new CustomNotFoundException("User profile not found for email: " + email));

        // Validate fee
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new CustomNotFoundException("Fee not found with ID: " + feeId));

        // Check for existing allocation
        if (hostelAllocationRepository.existsByProfileAndAcademicYearAndFee(profile, academicSession, fee)) {
            throw new EntityAlreadyExistException("Hostel allocation already exists for this student and session");
        }

        // Process payment
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .feeId(feeId)
                .amount(fee.getAmount())
                .build();
        paymentService.processPayment(paymentRequest);

        // Verify payment
        Payment payment = paymentRepository.findByStudentFeeAndProfileAndAcademicSession(fee, profile, academicSession)
                .orElseThrow(() -> new CustomInternalServerException("Payment processing failed"));

        // Create allocation
        HostelAllocation allocation = HostelAllocation.builder()
                .profile(profile)
                .fee(fee)
                .school(profile.getUser().getSchool())
                .academicYear(academicSession)
                .paymentStatus(PaymentStatus.SUCCESS)
                .allocationStatus(AllocationStatus.PENDING)
                .build();

        HostelAllocation savedAllocation = hostelAllocationRepository.save(allocation);
        return convertToResponse(savedAllocation);
    }

    @Override
    @Transactional
    public HostelAllocationResponse allocateStudentToHostel(HostelAllocationRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new AuthenticationFailedException("Please login as an Admin"));

        // Validate hostel
        Hostel hostel = hostelRepository.findById(request.getHostelId())
                .orElseThrow(() -> new CustomNotFoundException("Hostel not found with ID: " + request.getHostelId()));

        // Validate allocation
        HostelAllocation allocation = hostelAllocationRepository.findById(request.getAllocationId())
                .orElseThrow(() -> new CustomNotFoundException("Hostel allocation not found with ID: " + request.getAllocationId()));

        // Validate payment and status
        if (allocation.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Payment not completed for allocation ID: " + request.getAllocationId());
        }
        if (allocation.getAllocationStatus() == AllocationStatus.SUCCESS) {
            throw new EntityAlreadyExistException("Student already allocated to a hostel");
        }

        // Validate academic year
        AcademicSession academicYear = academicSessionRepository.findById(allocation.getAcademicYear().getId())
                .orElseThrow(() -> new CustomNotFoundException("Academic year not found"));

        // Check for existing allocation
        if (hostelAllocationRepository.existsByHostelAndProfileAndAcademicYearAndAllocationStatus(
                hostel, allocation.getProfile(), academicYear, AllocationStatus.SUCCESS)) {
            throw new EntityAlreadyExistException("Student already allocated to this hostel for the academic year");
        }

        // Validate bed number
        if (request.getBedNumber() <= 0 || request.getBedNumber() > hostel.getNumberOfBed()) {
            throw new IllegalArgumentException("Invalid bed number: " + request.getBedNumber());
        }
        if (hostelAllocationRepository.existsByHostelAndBedNumberAndAcademicYear(hostel, request.getBedNumber(), academicYear)) {
            throw new EntityAlreadyExistException("Bed number " + request.getBedNumber() + " is already taken");
        }

        // Manage bed tracker
        HostelBedTracker bedTracker = hostelBedTrackerRepository.findByHostelAndAcademicYear(hostel, academicYear)
                .orElseGet(() -> {
                    HostelBedTracker newTracker = HostelBedTracker.builder()
                            .hostel(hostel)
                            .academicYear(academicYear)
                            .bedsAllocated(0)
                            .numberOfBedLeft(hostel.getNumberOfBed())
                            .build();
                    return hostelBedTrackerRepository.save(newTracker);
                });

        // Check availability
        bedTracker.allocateBed(); // Throws if no beds available

        // Update allocation
        allocation.setHostel(hostel);
        allocation.setBedNumber(request.getBedNumber());
        allocation.setAllocationStatus(AllocationStatus.SUCCESS);

        // Save changes
        hostelBedTrackerRepository.save(bedTracker);
        HostelAllocation savedAllocation = hostelAllocationRepository.save(allocation);

        // Update hostel availability
        if (bedTracker.getNumberOfBedLeft() == 0) {
            hostel.setAvailabilityStatus(AvailabilityStatus.UNAVAILABLE);
            hostelRepository.save(hostel);
        }

        return convertToResponse(savedAllocation);
    }

    @Override
    public List<HostelAllocationResponse> getAllHostelAllocations() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));

        return hostelAllocationRepository.findBySchoolId(user.getSchool().getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<HostelAllocationResponse> getHostelAllocationById(Long id) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));

        return hostelAllocationRepository.findByIdAndSchoolId(id, user.getSchool().getId())
                .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public void deleteHostelAllocation(Long id) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new AuthenticationFailedException("Please login as an Admin"));

        HostelAllocation allocation = hostelAllocationRepository.findByIdAndSchoolId(id, admin.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("Hostel allocation not found with ID: " + id));

        if (allocation.getAllocationStatus() == AllocationStatus.SUCCESS) {
            HostelBedTracker tracker = hostelBedTrackerRepository.findByHostelAndAcademicYear(
                            allocation.getHostel(), allocation.getAcademicYear())
                    .orElseThrow(() -> new CustomNotFoundException("Bed tracker not found"));
            tracker.deallocateBed();
            hostelBedTrackerRepository.save(tracker);

            // Update hostel availability
            if (tracker.getNumberOfBedLeft() > 0 && allocation.getHostel().getAvailabilityStatus() == AvailabilityStatus.UNAVAILABLE) {
                allocation.getHostel().setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
                hostelRepository.save(allocation.getHostel());
            }
        }

        hostelAllocationRepository.delete(allocation);
    }

    @Override
    @Transactional
    public HostelAllocationResponse updatePaymentStatus(Long id, PaymentStatus paymentStatus) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new AuthenticationFailedException("Please login as an Admin"));

        HostelAllocation allocation = hostelAllocationRepository.findByIdAndSchoolId(id, admin.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("Hostel allocation not found with ID: " + id));

        PaymentStatus previousStatus = allocation.getPaymentStatus();
        if (previousStatus == paymentStatus) {
            throw new IllegalStateException("Payment status is already " + paymentStatus);
        }

        if (allocation.getAllocationStatus() == AllocationStatus.SUCCESS && paymentStatus != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot revoke payment for allocated bed");
        }

        allocation.setPaymentStatus(paymentStatus);
        HostelAllocation updated = hostelAllocationRepository.save(allocation);
        return convertToResponse(updated);
    }

    private HostelAllocationResponse convertToResponse(HostelAllocation allocation) {
        return HostelAllocationResponse.builder()
                .id(allocation.getId())
                .hostelId(allocation.getHostel() != null ? allocation.getHostel().getId() : null)
                .hostelName(allocation.getHostel() != null ? allocation.getHostel().getHostelName() : null)
                .bedNumber(allocation.getBedNumber())
                .profileId(allocation.getProfile().getId())
                .studentName(allocation.getProfile().getUser().getFirstName() + " " + allocation.getProfile().getUser().getLastName())
                .paymentStatus(allocation.getPaymentStatus())
                .allocationStatus(allocation.getAllocationStatus())
                .academicYearId(allocation.getAcademicYear().getId())
                .feeId(allocation.getFee().getId())
                .datestamp(allocation.getDatestamp())
                .build();
    }
}