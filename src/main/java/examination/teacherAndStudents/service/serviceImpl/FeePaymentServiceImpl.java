package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.EmailDetails;
import examination.teacherAndStudents.dto.PaymentRequest;
import examination.teacherAndStudents.dto.PaymentWithoutFeeIdRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.FeePaymentService;
import examination.teacherAndStudents.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class FeePaymentServiceImpl implements FeePaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FeeRepository feeRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StudentTermRepository studentTermRepository;


    public void processPayment(PaymentRequest paymentDTO) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile student = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        // Validate payment
        validatePayment(paymentDTO);

        // Get the fee
        Fee fee = feeRepository.findByIdAndSchoolId(paymentDTO.getFeeId(), student.getUser().getSchool().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee not found"));

        if (!isFeeApplicable(student, fee)) {
            throw new BadRequestException("This fee is not applicable to the student");
        }

        BigDecimal totalPaidBefore = getTotalPaymentsForFee(student, fee);
        BigDecimal remainingBalance = fee.getAmount().subtract(totalPaidBefore);

        // 4. Validate payment amount
        if (paymentDTO.getAmount().compareTo(remainingBalance) > 0) {
            throw new BadRequestException(String.format(
                    "Payment amount exceeds remaining balance of %s", remainingBalance));
        }

        // Get student's wallet
        Wallet wallet = walletRepository.findWalletByUserProfile(student)
                .orElseThrow(() -> new ResourceNotFoundException("Student wallet not found"));

        // Debit wallet
        wallet.debit(paymentDTO.getAmount());
        walletRepository.save(wallet);

        // Create payment record
        Payment payment = new Payment();
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentDate(LocalDate.now());
        payment.setMethod(PaymentMethod.BALANCE);
        payment.setFeeCategory(fee.getCategory());
        payment.setReferenceNumber(ReferenceGenerator.generateShortReference());
        payment.setTransactionId(ReferenceGenerator.generateTransactionId("BAL"));
        payment.setAcademicSession(fee.getSession());
        if (fee.getClassLevel() != null) {
            payment.setClassLevel(fee.getClassLevel());
        }
        if (fee.getSubClass() != null) {
            payment.setClassBlock(fee.getSubClass());
        }
        if (fee.getTerm() != null) {
            payment.setStudentTerm(fee.getTerm());
        }
        payment.setPaid(true);
        payment.setProfile(student); // Link payment to student
        payment.setStudentFee(fee); // Link payment to fee

        BigDecimal totalPaidAfter = totalPaidBefore.add(paymentDTO.getAmount());
        if (totalPaidAfter.compareTo(fee.getAmount()) >= 0) {
            payment.setStatus(FeeStatus.PAID);
        } else {
            payment.setStatus(FeeStatus.PARTIALLY_PAID);
        }
        boolean isFullyPaid = totalPaidAfter.compareTo(fee.getAmount()) >= 0;
        payment.setPaid(isFullyPaid);

        Transaction transaction = createTransaction(student, fee);
        sendPaymentNotifications(student, fee, transaction);
        transactionRepository.save(transaction);

        paymentRepository.save(payment);
    }




    @Override
    public void processPaymentWithoutFeeId(PaymentWithoutFeeIdRequest paymentDTO) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile student = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        // Get student's wallet
        Wallet wallet = walletRepository.findWalletByUserProfile(student)
                .orElseThrow(() -> new ResourceNotFoundException("Student wallet not found"));


        // Debit wallet
        wallet.debit(paymentDTO.getAmount());
        walletRepository.save(wallet);

        // Create payment record
        Payment payment = new Payment();
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentDate(LocalDate.now());
        payment.setMethod(PaymentMethod.BALANCE);
        payment.setReferenceNumber(ReferenceGenerator.generateShortReference());
        payment.setTransactionId(ReferenceGenerator.generateTransactionId("BAL"));
        payment.setPaid(true);
        payment.setFullyPaid(true);
        payment.setProfile(student);
        payment.setStatus(FeeStatus.PAID);


        Transaction transaction = createTransactionForFeesWithoutIds(student, paymentDTO);
        sendPaymentNotificationsForFeesWithoutIds(student, paymentDTO, transaction);
        transactionRepository.save(transaction);
        payment.setAcademicSession(transaction.getSession());
        payment.setClassBlock(transaction.getClassBlock());
        payment.setStudentTerm(transaction.getStudentTerm());
        payment.setClassLevel(transaction.getClassBlock().getClassLevel());
        payment.setPurpose(paymentDTO.getPurpose());
        paymentRepository.save(payment);
    }


    private BigDecimal getTotalPaymentsForFee(Profile student, Fee fee) {
        return paymentRepository.findByProfileAndStudentFee(student, fee).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isFeeApplicable(Profile student, Fee fee) {
        // Check school match
        if (!student.getUser().getSchool().getId().equals(fee.getSchool().getId())) {
            return false;
        }
        // Check class level if specified in fee
        if (fee.getClassLevel() != null &&
                !fee.getClassLevel().getId().equals(student.getSessionClass().getClassBlock().getClassLevel().getId())) {
            return false;
        }
        // Check subclass if specified
        if (fee.getSubClass() != null &&
                !fee.getSubClass().getClassLevel().getId().equals(student.getSessionClass().getClassBlock().getClassLevel().getId())) {
            return false;
        }

        return true;
    }

    private void validatePayment(PaymentRequest paymentDTO) {
        if (paymentDTO == null || paymentDTO.getAmount() == null) {
            throw new IllegalArgumentException("Invalid fee data");
        }
        if (paymentDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }

    private Transaction createTransactionForFeesWithoutIds(Profile profile, PaymentWithoutFeeIdRequest due) {

        StudentTerm studentTerm  = studentTermRepository.findCurrentTerm(LocalDate.now(), profile.getUser().getSchool().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student term not found"));

        return Transaction.builder()
                .transactionType(TransactionType.DEBIT)
                .user(profile)
                .status(TransacStatus.SUCCESS)
                .amount(due.getAmount())
                .studentTerm(studentTerm)
                .session(studentTerm.getAcademicSession())
                .classBlock(profile.getSessionClass().getClassBlock())
                .description(String.format("Payment for %s", due.getDescription()))
                .build();
    }

    private void sendPaymentNotificationsForFeesWithoutIds(Profile profile,
                                                           PaymentWithoutFeeIdRequest due, Transaction transaction) {
        // Create and save notification
        Notification notification = Notification.builder()
                .notificationType(NotificationType.DEBIT_NOTIFICATION)
                .user(profile)
                .title(due.getDescription())
                .notificationStatus(NotificationStatus.UNREAD)
                .transaction(transaction)
                .message(String.format("You have paid ₦%s for %s",
                        due.getAmount(), due.getDescription()))
                .build();
        notificationRepository.save(notification);

        // Send email
        Map<String, Object> emailModel = new HashMap<>();
        emailModel.put("amount", due.getAmount());
        emailModel.put("name", profile.getUser().getFirstName() + " " + profile.getUser().getLastName());
        emailModel.put("purpose", due.getDescription());

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(profile.getUser().getEmail())
                .subject("Payment Confirmation")
                .templateName("payment-confirmation")
                .model(emailModel)
                .build();

        emailService.sendEmails(emailDetails);
    }

    private Transaction createTransaction(Profile profile, Fee due) {
        return Transaction.builder()
                .transactionType(TransactionType.DEBIT)
                .user(profile)
                .status(TransacStatus.SUCCESS)
                .amount(due.getAmount())
                .studentTerm(due.getTerm())
                .session(due.getSession())
                .classBlock(due.getSubClass())
                .description(String.format("Payment for %s", due.getCategory().getName()))
                .build();
    }


    private void sendPaymentNotifications(Profile profile,
                                          Fee due, Transaction transaction) {
        // Create and save notification
        Notification notification = Notification.builder()
                .notificationType(NotificationType.DEBIT_NOTIFICATION)
                .user(profile)
                .title(due.getDescription())
                .notificationStatus(NotificationStatus.UNREAD)
                .transaction(transaction)
                .message(String.format("You have paid ₦%s for %s",
                        due.getAmount(), due.getCategory().getName()))
                .build();
        notificationRepository.save(notification);

        // Send email
        Map<String, Object> emailModel = new HashMap<>();
        emailModel.put("amount", due.getAmount());
        emailModel.put("name", profile.getUser().getFirstName() + " " + profile.getUser().getLastName());
        emailModel.put("purpose", due.getCategory().getName());

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(profile.getUser().getEmail())
                .subject("Payment Confirmation")
                .templateName("payment-confirmation")
                .model(emailModel)
                .build();

        emailService.sendEmails(emailDetails);
    }

}