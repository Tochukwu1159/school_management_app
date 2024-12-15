package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.DuePaymentRequest;
import examination.teacherAndStudents.dto.DuePaymentResponse;
import examination.teacherAndStudents.entity.DuePayment;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.repository.DuePaymentRepository;
import examination.teacherAndStudents.repository.DuesRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.DuePaymentService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.StudentTerm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DuePaymentServiceImpl implements DuePaymentService {

    private final DuePaymentRepository duePaymentRepository;
    private final DuesRepository duesRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    // Create or Update DuePayment
    @Transactional
    public DuePaymentResponse makeDuePayment(DuePaymentRequest duePaymentRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User student = userRepository.findByEmailAndRoles(email, Roles.STUDENT);
        if (student == null) {
            throw new AuthenticationFailedException("Please login as an student");
        }
        // Trigger the payment process
        paymentService.payDue(duePaymentRequest.getDueId(), duePaymentRequest.getTerm(), duePaymentRequest.getSessionId());

        // Retrieve the saved payment record
        DuePayment duePayment = duePaymentRepository.findByDueIdAndUserId(duePaymentRequest.getDueId(),student.getId());

        if (duePayment == null) {
            throw new EntityNotFoundException("Payment record not found after processing.");
        }
        // Return the payment response
        return new DuePaymentResponse(
                duePayment.getId(),
                duePayment.getDue().getId(),
                duePayment.getUser().getId(),
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
                duePayment.getUser().getId(),
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
                        duePayment.getUser().getId(),
                        duePayment.getPaymentStatus(),
                        duePayment.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Get all DuePayments by User
    public List<DuePaymentResponse> getAllDuePaymentsByUser(Long userId) {
        return duePaymentRepository.findByUserId(userId).stream()
                .map(duePayment -> new DuePaymentResponse(
                        duePayment.getId(),
                        duePayment.getDue().getId(),
                        duePayment.getUser().getId(),
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
