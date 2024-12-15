package examination.teacherAndStudents.service;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.HostelAllocationRequest;
import examination.teacherAndStudents.dto.HostelAllocationResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.TransactionType;
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

    @Transactional
    public HostelAllocationResponse payHotelAllocation(Long dueId, Long sessionId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User user = userRepository.findByEmailAndRoles(email, Roles.STUDENT);
            if (user == null) {
                throw new CustomNotFoundException("Please login as a Student");
            }
            // verify the session
            AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException("Academic session not found with id: " + sessionId));


            Dues hostelFee = duesRepository.findById(dueId)
                    .orElseThrow(() -> new EntityNotFoundException("Hostel Allocation not found"));

           paymentService.payDue(dueId, null, academicSession.getId() );
            // Step 3: Update payment status and save the allocation
            HostelAllocation hostelAllocation = new HostelAllocation();
            hostelAllocation.setPaymentStatus(PaymentStatus.SUCCESS);
            hostelAllocation.setUser(user);
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
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        HostelAllocation hostelAllocation = hostelAllocationRepository.findByIdAndUserId(request.getAllocationId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Hostel allocation not found"));

        if(hostelAllocation.getPaymentStatus() != PaymentStatus.SUCCESS){
            throw new EntityNotFoundException("Student has not paid yet");
        }
        Hostel hostel = hostelRepository.findById(request.getHostelId())
                .orElseThrow(() -> new RuntimeException("Hostel not found"));

        hostelAllocation.setHostel(hostel);
        hostelAllocation.setBedNumber(request.getBedNumber());


        hostelAllocation = hostelAllocationRepository.save(hostelAllocation);

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

        hostelAllocation.setPaymentStatus(paymentStatus);
        hostelAllocation = hostelAllocationRepository.save(hostelAllocation);

        return convertToResponse(hostelAllocation);
    }

    // Utility method to convert HostelAllocation to HostelAllocationResponse
    private HostelAllocationResponse convertToResponse(HostelAllocation hostelAllocation) {
        HostelAllocationResponse response = new HostelAllocationResponse();
        response.setId(hostelAllocation.getId());
        response.setUser(hostelAllocation.getUser());
        response.setHostel(hostelAllocation.getHostel());
        response.setBedNumber(hostelAllocation.getBedNumber());
        response.setPaymentStatus(hostelAllocation.getPaymentStatus());
        response.setDatestamp(hostelAllocation.getDatestamp());
        return response;
    }
}
