package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.EmailDetails;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.utils.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.rmi.AlreadyBoundException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;


    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private DuesRepository dueRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private DuesRepository duesRepository;
    @Autowired
    private DuesPaymentRepository duePaymentRepository;
    @Autowired
    private AcademicSessionRepository academicSessionRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private StudentTermRepository studentTermRepository;


    @Transactional
    public void payDue(Long dueId, Long termId, Long sessionId) {
        try {
            // Retrieve the authenticated user's email
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> user = userRepository.findByEmail(email);

            if (user == null) {
                throw new CustomNotFoundException("Please login as a Student");
            }
            Optional<Profile> userProfile = profileRepository.findByUser(user.get());

            if (userProfile.isEmpty()) {
                throw new CustomNotFoundException("Please login as a Student");
            }
            // Retrieve the user's wallet
            Wallet wallet = walletRepository.findWalletByUserProfile(userProfile.get());

            // verify the session
            AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException("Academic session not found with id: " + sessionId));

            // Find the due associated with the given ID
            Dues dues = dueRepository.findById(dueId)
                    .orElseThrow(() -> new EntityNotFoundException("Due not found with id: " + dueId));

            // Check if a payment has already been made for this due by the user
            DuePayment existingPayment;
            if (termId != null) {
                StudentTerm term = studentTermRepository.findById(termId)
                        .orElseThrow(() -> new EntityNotFoundException("Student term not found with id: " + termId));
                // Check if a payment exists using dueId, userId, and sessionId
                existingPayment = duePaymentRepository.findByDueAndProfileAndAcademicYearAndStudentTerm(dues, userProfile.get(), academicSession, term);
            } else {
                // Check if a payment exists using dueId and userId only
                existingPayment = duePaymentRepository.findByDueIdAndAcademicYearAndProfile(dues.getId(), academicSession,userProfile.get());
            }

            if (existingPayment != null) {
                throw new DuplicateDesignationException("Due payment already made for this student");
            }

            BigDecimal amountToPay = dues.getAmount();

            // Check if the wallet has sufficient funds
            if (wallet.getBalance().compareTo(amountToPay) < 0) {
                throw new InsufficientBalanceException("Insufficient funds in the wallet to pay the due");
            }
            Optional<StudentTerm> studentTerm = Optional.empty();
            if (termId != null) {
                studentTerm = studentTermRepository.findById(termId);
            }

            // Deduct the amount from the wallet and update the total sent amount
            wallet.setBalance(wallet.getBalance().subtract(amountToPay));
            wallet.setTotalMoneySent(wallet.getTotalMoneySent().add(amountToPay));
            walletRepository.save(wallet);

            // Create a new due payment entry
            DuePayment duePayment = DuePayment.builder()
                    .academicYear(academicSession)
                    .studentTerm(studentTerm.orElse(null))
                    .due(dues)
                    .profile(userProfile.get())
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .build();

            // Save the due payment
            duePaymentRepository.save(duePayment);

            // Create a new transaction for this payment
            Transaction transaction = Transaction.builder()
                    .transactionType(TransactionType.DEBIT)
                    .user(userProfile.get())
                    .amount(amountToPay)
                    .studentTerm(studentTerm.orElse(null))
                    .session(academicSession)
                    .description("You have successfully paid " + amountToPay + " for " + dues.getPurpose())
                    .build();

            transactionRepository.save(transaction);

            // Create a new notification for the user
            Notification notification = Notification.builder()
                    .notificationType(NotificationType.DEBIT_NOTIFICATION)
                    .user(userProfile.get())
                    .notificationStatus(NotificationStatus.UNREAD)
                    .transaction(transaction)
                    .message("You have paid ₦" + amountToPay + " for " + dues.getPurpose())
                    .build();

            notificationRepository.save(notification);

            // Send an email notification about the payment
            Map<String, Object> model = new HashMap<>();
            model.put("amount", amountToPay);
            model.put("name", user.get().getFirstName() + " " + user.get().getLastName());

            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(user.get().getEmail())
                    .subject("Wallet funding status")
                    .templateName("email-template-wallet")
                    .model(model)
                    .build();

            emailService.sendEmails(emailDetails);

        } catch (EntityNotFoundException e) {
            throw new NotFoundException("User or due not found: " + e.getMessage());
        } catch (DuplicateDesignationException e) {
            throw new DuplicateDesignationException("Payment already made: " + e.getMessage());
        } catch (DataAccessException e) {
            throw new ResponseStatusException("Database error while processing the payment: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException("An unexpected error occurred: " + e.getMessage());
        }
    }

}

