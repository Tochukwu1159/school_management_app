package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.PayStackPaymentService;
import examination.teacherAndStudents.service.WalletService;
import examination.teacherAndStudents.utils.*;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final EmailService emailService;

    private final PayStackPaymentService paymentService;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;

    private static final Logger logger = LoggerFactory.getLogger(TimetableServiceImpl.class);


    @Override
    public WalletResponse getProfileWalletBalance() {
        String email = SecurityConfig.getAuthenticatedUserEmail();

        Optional<Profile> profile = profileRepository.findByUserEmail(email);
        if (profile.isEmpty()) {
            throw new CustomNotFoundException("Student with Id " + profile.get().getUniqueRegistrationNumber() + " is not valid");
        }
        // Validate profile status
        AccountUtils.validateProfileStatus(profile.get());


        Wallet wallet =  walletRepository.findWalletByUserProfile(profile.get())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found or phone number missing"));


        return new WalletResponse(wallet.getBalance(), wallet.getTotalMoneySent());

    }


    @Transactional
    public PaymentResponse fundWallet(FundWalletRequest fundWalletRequest) {
        // Validate input
        validateFundRequest(fundWalletRequest);

        // Process payment
        PaymentResult paymentResult = processPayment(fundWalletRequest);

        // Handle wallet operations
        WalletOperationResult walletResult = handleWalletOperations(
                paymentResult.email(),
                paymentResult.amountInNaira(),
                paymentResult.transactionResponse()
        );

        // Send notifications
        sendNotifications(walletResult);

        return new PaymentResponse(paymentResult.authorizationUrl());
    }

    // Helper methods
    private void validateFundRequest(FundWalletRequest request) {
        if (request == null || request.getAmount() == null || request.getAmount().isEmpty()) {
            throw new IllegalArgumentException("Invalid funding request");
        }

        try {
            double amount = Double.parseDouble(request.getAmount());
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        }
    }

    private PaymentResult processPayment(FundWalletRequest request) {
        try {
            int amountInKobo = (int) (Double.parseDouble(request.getAmount()) * 100);
            String email = SecurityConfig.getAuthenticatedUserEmail();

            PayStackTransactionRequest paymentRequest = PayStackTransactionRequest.builder()
                    .email(email)
                    .amount(BigDecimal.valueOf(amountInKobo))
                    .build();

            PayStackTransactionResponse response = paymentService.initTransaction(paymentRequest);

            if (!response.isStatus()) {
                throw new PaymentProcessingException("Payment initialization failed: " + response.getMessage());
            }

            return new PaymentResult(
                    email,
                    Double.parseDouble(request.getAmount()),
                    response,
                    response.getData().getAuthorization_url()
            );
        } catch (Exception e) {
            throw new PaymentProcessingException("Payment processing error " +e);
        }
    }

    private WalletOperationResult handleWalletOperations(String email, double amount,
                                                         PayStackTransactionResponse transactionResponse) {

        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Profile not found for: " + email));

        Wallet wallet = walletRepository.findWalletByUserProfile(profile)
                .orElseGet(() -> createNewWallet(profile));

        BigDecimal amountDecimal = BigDecimal.valueOf(amount);
        wallet.credit(amountDecimal);
        walletRepository.save(wallet);

        Transaction transaction = createTransaction(profile, amountDecimal, transactionResponse);
        transactionRepository.save(transaction);

        return new WalletOperationResult(profile.getUser(), profile, wallet, transaction, amount);
    }

    private Wallet createNewWallet(Profile profile) {
        return walletRepository.save(
                Wallet.builder()
                        .userProfile(profile)
                        .balance(BigDecimal.ZERO)
                        .totalMoneyReceived(BigDecimal.ZERO)
                        .build()
        );
    }

    private Transaction createTransaction(Profile profile, BigDecimal amount,
                                          PayStackTransactionResponse response) {
        return Transaction.builder()
                .transactionType(TransactionType.CREDIT)
                .user(profile)
                .amount(amount)
                .description(response.getMessage())
                .status(TransacStatus.SUCCESS)
                .paymentReference(response.getData().getReference())
                .build();
    }

    private void sendNotifications(WalletOperationResult result) {
        try {
            createNotification(result);
            sendEmailNotification(result);
        } catch (Exception e) {
            logger.error("Failed to send notifications for wallet funding", e);
        }
    }

    private void createNotification(WalletOperationResult result) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        String message = String.format(
                "You funded your wallet with ₦%s",
                formatter.format(result.amount())
        );

        Notification notification = Notification.builder()
                .notificationType(NotificationType.CREDIT_NOTIFICATION)
                .user(result.profile())
                .notificationStatus(NotificationStatus.UNREAD)
                .transaction(result.transaction())
                .message(message)
                .build();

        notificationRepository.save(notification);
    }

    private void sendEmailNotification(WalletOperationResult result) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        model.put("amount", result.amount());
        model.put("name", result.user().getFirstName() + " " + result.user().getLastName());
        model.put("balance", result.wallet().getBalance());

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(result.user().getEmail())
                .subject("Wallet Funding Confirmation")
                .templateName("wallet-funding-confirmation")
                .model(model)
                .build();

        emailService.sendHtmlEmail(emailDetails);
    }

    // Record classes for better data structure
    private record PaymentResult(
            String email,
            double amountInNaira,
            PayStackTransactionResponse transactionResponse,
            String authorizationUrl
    ) {}

    private record WalletOperationResult(
            User user,
            Profile profile,
            Wallet wallet,
            Transaction transaction,
            double amount
    ) {}


    public SchoolBalanceResponse schoolTotalWallet() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> admin = userRepository.findByEmail(email);
            if (admin.isEmpty()) {
                throw new CustomNotFoundException("Admin is not valid");
            }

            List<Wallet> wallets = walletRepository.findAll();
            if (wallets.isEmpty()) {
                throw new CustomNotFoundException("No wallets found");
            }

            BigDecimal totalBalance = BigDecimal.ZERO;
            BigDecimal totalMoneySent = BigDecimal.ZERO;
            BigDecimal totalFunds = BigDecimal.ZERO;
            for (Wallet wallet : wallets) {
                totalBalance = totalBalance.add(wallet.getBalance());
                totalMoneySent = totalMoneySent.add(wallet.getTotalMoneySent());
                totalFunds = totalFunds.add(wallet.getBalance()).add(wallet.getTotalMoneySent());
            }

            // Create and return the wallet response
            return SchoolBalanceResponse.builder()
                    .TotalMoneyFunded(totalFunds)
                    .balance(totalBalance)
                    .totalStudentMoneySent(totalMoneySent)
                    .build();
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching school wallet balance"+ e);
        }
    }
    }




