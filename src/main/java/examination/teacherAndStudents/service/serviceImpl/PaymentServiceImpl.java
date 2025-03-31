package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.EmailDetails;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.service.WalletService;
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
    private DuesPaymentRepository duePaymentRepository;
    @Autowired
    private AcademicSessionRepository academicSessionRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private StudentTermRepository studentTermRepository;

    @Autowired
    private WalletService walletService;



    @Transactional
    public PaymentResult payDue(Long dueId, Long termId, Long sessionId) {
        // Validate inputs
        validateInputs(dueId, sessionId);

        // Get authenticated user with proper validation
        User user = getAuthenticatedUser();
        Profile profile = getStudentProfile(user);
        Wallet wallet = getWallet(profile);

        // Retrieve academic entities
        AcademicSession session = getAcademicSession(sessionId);
        Dues due = getDue(dueId);
        StudentTerm term = getStudentTermIfProvided(termId);

        // Check for existing payment
        validateNoExistingPayment(due, profile, session, term);

        // Process payment
        return processPayment(user, profile, wallet, due, session, term);
    }

    // Helper methods
    private void validateInputs(Long dueId, Long sessionId) {
        if (dueId == null) {
            throw new IllegalArgumentException("Due ID cannot be null");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
    }

    private User getAuthenticatedUser() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AttendanceAlreadyTakenException("User not found. Please login"));
    }

    private Profile getStudentProfile(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new AttendanceAlreadyTakenException("Only students can make payments"));
    }

    private Wallet getWallet(Profile profile) {
        return walletRepository.findWalletByUserProfile(profile)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    }

    private AcademicSession getAcademicSession(Long sessionId) {
        return academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Academic session not found with ID: %d", sessionId)));
    }

    private Dues getDue(Long dueId) {
        return dueRepository.findById(dueId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Due not found with ID: %d", dueId)));
    }

    private StudentTerm getStudentTermIfProvided(Long termId) {
        if (termId == null) {
            return null;
        }
        return studentTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Student term not found with ID: %d", termId)));
    }

    private void validateNoExistingPayment(Dues due, Profile profile,
                                           AcademicSession session, StudentTerm term) {
        boolean paymentExists = term != null
                ? duePaymentRepository.existsByDueAndProfileAndAcademicYearAndStudentTerm(
                due, profile, session, term)
                : duePaymentRepository.existsByDueIdAndAcademicYearAndProfile(
                due.getId(), session, profile);

        if (paymentExists) {
            throw new DuplicatePaymentException(
                    String.format("Payment already made for due: %s", due.getPurpose()));
        }
    }

    private PaymentResult processPayment(User user, Profile profile, Wallet wallet,
                                         Dues due, AcademicSession session, StudentTerm term) {
        // Verify sufficient balance
        if (wallet.getBalance().compareTo(due.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient balance. Required: %s, Available: %s",
                            due.getAmount(), wallet.getBalance()));
        }

        // Deduct from wallet
        wallet.debit(due.getAmount());;
        walletRepository.save(wallet);

        // Create payment record
        DuePayment payment = createDuePayment(due, profile, session, term);
        duePaymentRepository.save(payment);

        // Record transaction
        Transaction transaction = createTransaction(user, profile, due, session, term);
        transactionRepository.save(transaction);

        // Send notifications
        sendPaymentNotifications(user, profile, due, transaction);

        return new PaymentResult(
                payment.getId(),
                transaction.getId(),
                due.getAmount(),
                "Payment successful"
        );
    }

    private DuePayment createDuePayment(Dues due, Profile profile,
                                        AcademicSession session, StudentTerm term) {
        return DuePayment.builder()
                .academicYear(session)
                .studentTerm(term)
                .due(due)
                .profile(profile)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
    }

    private Transaction createTransaction(User user, Profile profile, Dues due,
                                          AcademicSession session, StudentTerm term) {
        return Transaction.builder()
                .transactionType(TransactionType.DEBIT)
                .user(profile)
                .amount(due.getAmount())
                .studentTerm(term)
                .session(session)
                .description(String.format("Payment for %s", due.getPurpose()))
                .build();
    }

    private void sendPaymentNotifications(User user, Profile profile,
                                          Dues due, Transaction transaction) {
        // Create and save notification
        Notification notification = Notification.builder()
                .notificationType(NotificationType.DEBIT_NOTIFICATION)
                .user(profile)
                .notificationStatus(NotificationStatus.UNREAD)
                .transaction(transaction)
                .message(String.format("You have paid ₦%s for %s",
                        due.getAmount(), due.getPurpose()))
                .build();
        notificationRepository.save(notification);

        // Send email
        Map<String, Object> emailModel = new HashMap<>();
        emailModel.put("amount", due.getAmount());
        emailModel.put("name", user.getFirstName() + " " + user.getLastName());
        emailModel.put("purpose", due.getPurpose());

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(user.getEmail())
                .subject("Payment Confirmation")
                .templateName("payment-confirmation")
                .model(emailModel)
                .build();

        emailService.sendEmails(emailDetails);
    }

    // Record classes for better data structure
    public record PaymentResult(
            Long paymentId,
            Long transactionId,
            BigDecimal amount,
            String message
    ) {}

    public class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    public class DuplicatePaymentException extends RuntimeException {
        public DuplicatePaymentException(String message) {
            super(message);
        }
    }


//    @Transactional
//    public void payDue(Long dueId, Long termId, Long sessionId) {
//        try {
//            // Retrieve the authenticated user's email
//            String email = SecurityConfig.getAuthenticatedUserEmail();
//            Optional<Profile> userProfile = profileRepository.findByUserEmail(email);
//
//            if (userProfile.isEmpty()) {
//                throw new CustomNotFoundException("Please login as a Student");
//            }
//            // Retrieve the user's wallet
//            Wallet wallet = walletRepository.findWalletByUserProfile(userProfile.get());
//
//            // verify the session
//            AcademicSession academicSession = academicSessionRepository.findById(sessionId)
//                    .orElseThrow(() -> new EntityNotFoundException("Academic session not found with id: " + sessionId));
//
//            // Find the due associated with the given ID
//            Dues dues = dueRepository.findById(dueId)
//                    .orElseThrow(() -> new EntityNotFoundException("Due not found with id: " + dueId));
//
//            // Check if a payment has already been made for this due by the user
//            DuePayment existingPayment;
//            if (termId != null) {
//                StudentTerm term = studentTermRepository.findById(termId)
//                        .orElseThrow(() -> new EntityNotFoundException("Student term not found with id: " + termId));
//                // Check if a payment exists using dueId, userId, and sessionId
//                existingPayment = duePaymentRepository.findByDueAndProfileAndAcademicYearAndStudentTerm(dues, userProfile.get(), academicSession, term);
//            } else {
//                // Check if a payment exists using dueId and userId only
//                existingPayment = duePaymentRepository.findByDueIdAndAcademicYearAndProfile(dues.getId(), academicSession,userProfile.get());
//            }
//
//            if (existingPayment != null) {
//                throw new DuplicateDesignationException("Due payment already made for this student");
//            }
//
//            BigDecimal amountToPay = dues.getAmount();
//
//            // Check if the wallet has sufficient funds
//            if (wallet.getBalance().compareTo(amountToPay) < 0) {
//                throw new InsufficientBalanceException("Insufficient funds in the wallet to pay the due");
//            }
//            Optional<StudentTerm> studentTerm = Optional.empty();
//            if (termId != null) {
//                studentTerm = studentTermRepository.findById(termId);
//            }
//
//            // Deduct the amount from the wallet and update the total sent amount
//            wallet.setBalance(wallet.getBalance().subtract(amountToPay));
//            wallet.setTotalMoneySent(wallet.getTotalMoneySent().add(amountToPay));
//            walletRepository.save(wallet);
//
//            // Create a new due payment entry
//            DuePayment duePayment = DuePayment.builder()
//                    .academicYear(academicSession)
//                    .studentTerm(studentTerm.orElse(null))
//                    .due(dues)
//                    .profile(userProfile.get())
//                    .paymentStatus(PaymentStatus.SUCCESS)
//                    .build();
//
//            // Save the due payment
//            duePaymentRepository.save(duePayment);
//
//            // Create a new transaction for this payment
//            Transaction transaction = Transaction.builder()
//                    .transactionType(TransactionType.DEBIT)
//                    .user(userProfile.get())
//                    .amount(amountToPay)
//                    .studentTerm(studentTerm.orElse(null))
//                    .session(academicSession)
//                    .description("You have successfully paid " + amountToPay + " for " + dues.getPurpose())
//                    .build();
//
//            transactionRepository.save(transaction);
//
//            // Create a new notification for the user
//            Notification notification = Notification.builder()
//                    .notificationType(NotificationType.DEBIT_NOTIFICATION)
//                    .user(userProfile.get())
//                    .notificationStatus(NotificationStatus.UNREAD)
//                    .transaction(transaction)
//                    .message("You have paid ₦" + amountToPay + " for " + dues.getPurpose())
//                    .build();
//
//            notificationRepository.save(notification);
//
//            // Send an email notification about the payment
//            Map<String, Object> model = new HashMap<>();
//            model.put("amount", amountToPay);
//            model.put("name", userProfile.get().getUser().getFirstName() + " " + userProfile.get().getUser().getLastName());
//
//            EmailDetails emailDetails = EmailDetails.builder()
//                    .recipient(userProfile.get().getUser().getEmail())
//                    .subject("Wallet funding status")
//                    .templateName("email-template-wallet")
//                    .model(model)
//                    .build();
//
//            emailService.sendEmails(emailDetails);
//
//        } catch (EntityNotFoundException e) {
//            throw new NotFoundException("User or due not found: " + e.getMessage());
//        } catch (DuplicateDesignationException e) {
//            throw new DuplicateDesignationException("Payment already made: " + e.getMessage());
//        } catch (DataAccessException e) {
//            throw new ResponseStatusException("Database error while processing the payment: " + e.getMessage());
//        } catch (Exception e) {
//            throw new ResponseStatusException("An unexpected error occurred: " + e.getMessage());
//        }
//    }

}

