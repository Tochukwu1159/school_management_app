package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.DuePaymentRequest;
import examination.teacherAndStudents.dto.DuePaymentResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.DuePaymentService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.Roles;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class DuePaymentServiceImpl implements DuePaymentService {

    private final DuesPaymentRepository duePaymentRepository;
    private final DuesRepository duesRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final ProfileRepository profileRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;

    // Create or Update DuePayment
    @Transactional
    public DuePaymentResponse makeDuePayment(DuePaymentRequest duePaymentRequest) {
        // Authentication and validation
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User student = userRepository.findByEmailAndRoles(email, Roles.STUDENT)
                .orElseThrow(() -> new AuthenticationFailedException("Student access required"));

        Profile studentProfile = profileRepository.findByUser(student)
                .orElseThrow(() -> new EntityNotFoundException("Student profile not found"));

        // Validate request
        if (duePaymentRequest == null || duePaymentRequest.getDueId() == null || duePaymentRequest.getSessionId() == null) {
            throw new IllegalArgumentException("Invalid payment request");
        }

        // Fetch required entities
        Dues dues = duesRepository.findById(duePaymentRequest.getDueId())
                .orElseThrow(() -> new EntityNotFoundException("Dues not found"));

        AcademicSession academicSession = academicSessionRepository.findById(duePaymentRequest.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Academic session not found"));

        // Verify school consistency
        if (!dues.getSchool().getId().equals(student.getSchool().getId()) ||
                !academicSession.getSchool().getId().equals(student.getSchool().getId())) {
            throw new AuthenticationFailedException("Payment not allowed for this school");
        }

        // Process payment
        PaymentServiceImpl.PaymentResult paymentResult = paymentService.payDue(
                duePaymentRequest.getDueId(),
                duePaymentRequest.getTerm(),
                duePaymentRequest.getSessionId()
        );

        // Create or update payment record
        DuePayment duePayment = duePaymentRepository.findByDueAndSchoolIdAndProfileAndAcademicYear(dues, studentProfile.getUser().getSchool().getId(), studentProfile, academicSession)
                .orElseGet(() -> new DuePayment());

        duePayment.setDue(dues);
        duePayment.setProfile(studentProfile);
        duePayment.setAcademicYear(academicSession);
        duePayment.setSchool(studentProfile.getUser().getSchool());
        duePayment.setStudentTerm(duePaymentRequest.getTerm() != null ?
                studentTermRepository.findById(duePaymentRequest.getTerm()).orElse(null) : null);
        duePayment.setPaymentStatus(PaymentStatus.SUCCESS);
//        duePayment.setTransactionId(paymentResult.getTransactionId());
//        duePayment.setAmountPaid(dues.getAmount());
//        duePayment.setPaymentDate(LocalDateTime.now());

        DuePayment savedPayment = duePaymentRepository.save(duePayment);

        // Return detailed response
        return DuePaymentResponse.builder()
                .id(savedPayment.getId())
                .dueId(savedPayment.getDue().getId())
                .duePurpose(savedPayment.getDue().getPurpose())
                .profileId(savedPayment.getProfile().getId())
                .profileName(savedPayment.getProfile().getUser().getFirstName() + " " + savedPayment.getProfile().getUser().getLastName())
//                .amountPaid(savedPayment.getAmountPaid())
                .paymentStatus(savedPayment.getPaymentStatus())
//                .transactionId(savedPayment.getTransactionId())
//                .paymentDate(savedPayment.getPaymentDate())
                .createdAt(savedPayment.getCreatedAt())
                .build();
    }

    // Get DuePayment by ID
    public DuePaymentResponse getDuePaymentById(Long id) {
        DuePayment duePayment = duePaymentRepository.findById(id).orElseThrow(() -> new RuntimeException("DuePayment not found"));

        return DuePaymentResponse.builder()
                .id(duePayment.getId())
                .duePurpose(duePayment.getPaymentStatus().name())
                .createdAt(duePayment.getCreatedAt())
                .dueId(duePayment.getDue().getId())
                .profileId(duePayment.getProfile().getId())
                .build();
    }

    @Override
    public Page<DuePaymentResponse> getAllDuePaymentsByUser(
            Long userId,
            Long dueId,
            Long studentTermId,
            Long academicYearId,
            LocalDateTime createdAt,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();

            Profile userProfile = profileRepository.findByUserEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("User profile not found"));

            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Fetch filtered due payments
            Page<DuePayment> paymentsPage = duePaymentRepository.findAllByUserWithFilters(
                    userId,
                    dueId,
                    studentTermId,
                    academicYearId,
                    createdAt,
                    pageable);

            // Map to response DTO
            return paymentsPage.map(this::mapToResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching due payments: " + e.getMessage());
        }
    }


    // Get all DuePayments
    public Page<DuePaymentResponse> getAllDuePayments(
            Long id,
            Long studentTermId,
            Long academicYearId,
            Long profileId,
            Long dueId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Fetch filtered due payments
            Page<DuePayment> paymentsPage = duePaymentRepository.findAllBySchoolWithFilters(
                    admin.getSchool().getId(),
                    id,
                    studentTermId,
                    academicYearId,
                    profileId,
                    dueId,
                    startDate,
                    endDate,
                    pageable);

            // Map to response DTO
            return paymentsPage.map(this::mapToResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching due payments: " + e.getMessage());
        }
    }

    private DuePaymentResponse mapToResponse(DuePayment duePayment) {
        return DuePaymentResponse.builder()
                .id(duePayment.getId())
                .dueId(duePayment.getDue().getId())
                .duePurpose(duePayment.getDue().getPurpose()) // Added due purpose
                .profileId(duePayment.getProfile().getId())
                .profileName(duePayment.getProfile().getUser().getFirstName() + " " + duePayment.getProfile().getUser().getLastName())
                .studentTermId(duePayment.getDue().getStudentTerm() != null ?
                        duePayment.getDue().getStudentTerm().getId() : null)
                .studentTermName(duePayment.getDue().getStudentTerm() != null ?
                        duePayment.getDue().getStudentTerm().getName() : null)
                .academicYearId(duePayment.getDue().getAcademicYear().getId())
                .academicYearName(duePayment.getDue().getAcademicYear().getName())
                .paymentStatus(duePayment.getPaymentStatus())
                .amount(duePayment.getDue().getAmount())
                .createdAt(duePayment.getCreatedAt())
                .updatedAt(duePayment.getUpdatedAt())
                .build();
    }



    // Delete DuePayment by ID
    public void deleteDuePaymentById(Long id) {
        DuePayment duePayment = duePaymentRepository.findById(id).orElseThrow(() -> new RuntimeException("DuePayment not found"));
        duePaymentRepository.delete(duePayment);
    }
}
