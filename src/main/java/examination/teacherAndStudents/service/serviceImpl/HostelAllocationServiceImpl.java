package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.HostelAllocationRequest;
import examination.teacherAndStudents.dto.HostelAllocationResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.HostelAllocationService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.utils.AllocationStatus;
import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.TransactionType;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HostelAllocationServiceImpl implements HostelAllocationService {

    private final HostelAllocationRepository hostelAllocationRepository;
    private final HostelRepository hostelRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentService paymentService;
    private final DuesRepository duesRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final HostelBedTrackerRepository hostelBedTrackerRepository;
    private final ProfileRepository profileRepository;
    private final DuesPaymentRepository duesPaymentRepository;

    @Transactional
    public HostelAllocationResponse payHotelAllocation(Long dueId, Long sessionId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> user = userRepository.findByEmail(email);
            if (user == null) {
                throw new CustomNotFoundException("Please login as a Student");
            }
            // verify the session
            AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException("Academic session not found with id: " + sessionId));

            Profile profile = profileRepository.findByUser(user.get())
                    .orElseThrow(() -> new EntityNotFoundException("User profile not found for ID : " + sessionId));

            DuePayment duePayment = duesPaymentRepository
                    .findByDueIdAndAcademicYearAndProfile(dueId,academicSession,  profile);
            if(duePayment!= null) {
                throw new CustomInternalServerException("You have paid for transport for the month");
            }

            HostelAllocation existingAllocation = hostelAllocationRepository.findByDuesIdAndProfile(dueId, profile);
            if (existingAllocation != null) {
                throw new EntityExistsException("Hostel payment already made, please wait until hostel allocation finished");

            }

            paymentService.payDue(dueId, null, academicSession.getId() );
            // Step 3: Update payment status and save the allocation
            HostelAllocation hostelAllocation = new HostelAllocation();
            hostelAllocation.setPaymentStatus(PaymentStatus.SUCCESS);
            hostelAllocation.setAllocationStatus(AllocationStatus.PENDING);
            hostelAllocation.setDues(duesRepository.findById(dueId).get());
            hostelAllocation.setAcademicYear(academicSession);
            hostelAllocation.setProfile(profile);
            hostelAllocationRepository.save(hostelAllocation);

            // Step 4: Return updated response
            return convertToResponse(hostelAllocation);

        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Hostel Allocation not found: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException("Error processing payment: " + e.getMessage());
        }
    }


    @Transactional
    public HostelAllocationResponse allocateStudentToHostel(HostelAllocationRequest request) {

        User student = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Profile userProfile = profileRepository.findByUser(student)
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        Hostel hostel = hostelRepository.findById(request.getHostelId())
                .orElseThrow(() -> new NotFoundException("Hostel not found"));

        AcademicSession academicYear = academicSessionRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new NotFoundException("Academic year not found"));

        HostelAllocation hostelAllocation = hostelAllocationRepository.findByIdAndAcademicYearAndProfile(request.getAllocationId(),academicYear, userProfile);
        if (hostelAllocation == null) {
            throw new NotFoundException(
                    "Hostel allocation does not exist, or allocation belongs to another student");
        }


        HostelAllocation existingAllocation1 = hostelAllocationRepository.findByHostelAndProfileAndAcademicYearAndAllocationStatus(hostel, userProfile, academicYear, AllocationStatus.SUCCESS);
        if (existingAllocation1 != null) {
            throw new AttendanceAlreadyTakenException(
                    "Student has already been allocated a hostel space for this academic year");
        }
        HostelAllocation existingAllocationForBed = hostelAllocationRepository
                .findByHostelAndBedNumberAndAcademicYear(hostel, request.getBedNumber(), academicYear);
        if (existingAllocationForBed != null) {
            throw new CustomUserAlreadyRegistered("Bed number " + request.getBedNumber() + " is already taken in this hostel.");
        }

        if (request.getAllocationId() != null) {
            HostelAllocation existingAllocation = hostelAllocationRepository.findById(request.getAllocationId())
                    .orElseThrow(() -> new NotFoundException("Hostel allocation not found"));

            if (!existingAllocation.getProfile().equals(userProfile)) {
                throw new NotFoundException("Allocation ID belongs to another student");
            }
            if (existingAllocation.getPaymentStatus() != PaymentStatus.SUCCESS) {
                throw new RuntimeException("Payment not made for the allocation");
            }
        }

        HostelBedTracker bedTracker = hostelBedTrackerRepository.findByHostelAndAcademicYear(hostel, academicYear)
                .orElseGet(() -> hostelBedTrackerRepository.save(
                        HostelBedTracker.builder()
                                .hostel(hostel)
                                .academicYear(academicYear)
                                .bedsAllocated(0)
                                .build()
                ));

        if (bedTracker.getBedsAllocated() >= hostel.getNumberOfBed()) {
            throw new NotFoundException("No beds available in this hostel for the selected academic year");
        }

        hostelAllocation.setHostel(hostel);
        hostelAllocation.setBedNumber(request.getBedNumber());
        hostelAllocation.setAllocationStatus(AllocationStatus.SUCCESS);
        hostelAllocationRepository.save(hostelAllocation);

        bedTracker.setBedsAllocated(bedTracker.getBedsAllocated() + 1);
        hostelBedTrackerRepository.save(bedTracker);

        return convertToResponse(hostelAllocation);
    }


    public List<HostelAllocationResponse> getAllHostelAllocations() {
        return hostelAllocationRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Optional<HostelAllocationResponse> getHostelAllocationById(Long id) {
        return hostelAllocationRepository.findById(id)
                .map(this::convertToResponse);
    }

    @Transactional
    public void deleteHostelAllocation(Long id) {
        HostelAllocation hostelAllocation = hostelAllocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hostel allocation not found"));
        hostelAllocationRepository.delete(hostelAllocation);
    }

    @Transactional
    public HostelAllocationResponse updatePaymentStatus(Long id, PaymentStatus paymentStatus) {
        HostelAllocation hostelAllocation = hostelAllocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hostel allocation not found"));

        PaymentStatus previousStatus = hostelAllocation.getPaymentStatus();

        if (previousStatus == PaymentStatus.SUCCESS && paymentStatus != PaymentStatus.SUCCESS) {
            // Decrease the allocated beds count if payment is revoked
            HostelBedTracker bedTracker = hostelBedTrackerRepository.findByHostelAndAcademicYear(
                            hostelAllocation.getHostel(), hostelAllocation.getAcademicYear())
                    .orElseThrow(() -> new RuntimeException("Bed tracker not found"));

            if (bedTracker.getBedsAllocated() > 0) {
                bedTracker.setBedsAllocated(bedTracker.getBedsAllocated() - 1);
                hostelBedTrackerRepository.save(bedTracker);
            }
        } else if (previousStatus != PaymentStatus.SUCCESS && paymentStatus == PaymentStatus.SUCCESS) {
            // Increase allocated beds if payment is successful
            HostelBedTracker bedTracker = hostelBedTrackerRepository.findByHostelAndAcademicYear(
                            hostelAllocation.getHostel(), hostelAllocation.getAcademicYear())
                    .orElseThrow(() -> new RuntimeException("Bed tracker not found"));

            if (bedTracker.getBedsAllocated() >= hostelAllocation.getHostel().getNumberOfBed()) {
                throw new RuntimeException("No beds available in this hostel for the selected academic year");
            }
            bedTracker.setBedsAllocated(bedTracker.getBedsAllocated() + 1);
            hostelBedTrackerRepository.save(bedTracker);
        }

        hostelAllocation.setPaymentStatus(paymentStatus);
        hostelAllocation = hostelAllocationRepository.save(hostelAllocation);

        return convertToResponse(hostelAllocation);
    }


    // Utility method to convert HostelAllocation to HostelAllocationResponse
    private HostelAllocationResponse convertToResponse(HostelAllocation hostelAllocation) {
        HostelAllocationResponse response = new HostelAllocationResponse();
        response.setId(hostelAllocation.getId());
//        response.setUser(hostelAllocation.getUser());
//        response.setHostel(hostelAllocation.getHostel());
//        response.setBedNumber(hostelAllocation.getBedNumber());
//        response.setPaymentStatus(hostelAllocation.getPaymentStatus());
        response.setDatestamp(hostelAllocation.getDatestamp());
        return response;
    }
}
