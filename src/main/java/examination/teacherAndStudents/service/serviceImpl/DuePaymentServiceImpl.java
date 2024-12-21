package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.DuePaymentRequest;
import examination.teacherAndStudents.dto.DuePaymentResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.DuePaymentService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.utils.Roles;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public DuePaymentResponse makeDuePayment(DuePaymentRequest duePaymentRequest) {
        // Retrieve the authenticated user's email and verify their role
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User student = userRepository.findByEmailAndRoles(email, Roles.STUDENT);
        if (student == null) {
            throw new AuthenticationFailedException("Please login as a student");
        }

        // Retrieve the student's profile
        Profile userProfile = profileRepository.findByUser(student)
                .orElseThrow(() -> new CustomNotFoundException("Please login as a student"));

        // Trigger the payment process
        paymentService.payDue(duePaymentRequest.getDueId(), duePaymentRequest.getTerm(), duePaymentRequest.getSessionId());

        // Retrieve associated entities for due payment
        Dues dues = duesRepository.findById(duePaymentRequest.getDueId())
                .orElseThrow(() -> new EntityNotFoundException("Dues not found with id: " + duePaymentRequest.getDueId()));

        AcademicSession academicSession = academicSessionRepository.findById(duePaymentRequest.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Academic session not found with id: " + duePaymentRequest.getSessionId()));

        Optional<StudentTerm> term = Optional.empty();
        if (duePaymentRequest.getTerm() != null) {
            term = studentTermRepository.findById(duePaymentRequest.getTerm());
        }

        // Check if the payment already exists
        DuePayment duePayment = term.isPresent()
                ? duePaymentRepository.findByDueAndProfileAndAcademicYearAndStudentTerm(dues, userProfile, academicSession, term.get())
                : duePaymentRepository.findByDueIdAndAcademicYearAndProfile(dues.getId(), academicSession, userProfile);

        if (duePayment == null) {
            throw new NotFoundException("Payment not found for this student or not processed");
        }

        // Return the payment response
        return new DuePaymentResponse(
                duePayment.getId(),
                duePayment.getDue().getId(),
                duePayment.getProfile().getId(),
                duePayment.getPaymentStatus(),
                duePayment.getCreatedAt()
        );
    }


    // Get DuePayment by ID
    public DuePaymentResponse getDuePaymentById(Long id) {
        DuePayment duePayment = duePaymentRepository.findById(id).orElseThrow(() -> new RuntimeException("DuePayment not found"));
        return new DuePaymentResponse(
                duePayment.getId(),
                duePayment.getDue().getId(),
                duePayment.getProfile().getId(),
                duePayment.getPaymentStatus(),
                duePayment.getCreatedAt()
        );
    }

    // Get all DuePayments
    public List<DuePaymentResponse> getAllDuePayments() {
        return duePaymentRepository.findAll().stream()
                .map(duePayment -> new DuePaymentResponse(
                        duePayment.getId(),
                        duePayment.getDue().getId(),
                        duePayment.getProfile().getId(),
                        duePayment.getPaymentStatus(),
                        duePayment.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Get all DuePayments by User
    public List<DuePaymentResponse> getAllDuePaymentsByUser(Long userId) {
        Optional<Profile> student = profileRepository.findByUserId(userId);
        return duePaymentRepository.findByProfileId(student.get().getId()).stream()
                .map(duePayment -> new DuePaymentResponse(
                        duePayment.getId(),
                        duePayment.getDue().getId(),
                        duePayment.getProfile().getId(),
                        duePayment.getPaymentStatus(),
                        duePayment.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Delete DuePayment by ID
    public void deleteDuePaymentById(Long id) {

        DuePayment duePayment = duePaymentRepository.findById(id).orElseThrow(() -> new RuntimeException("DuePayment not found"));
        duePaymentRepository.delete(duePayment);
    }
}
