package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.*;
import examination.teacherAndStudents.service.funding.PaymentProvider;
import examination.teacherAndStudents.service.funding.PaymentProviderFactory;
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
    private final PaymentProviderFactory paymentProviderFactory;
    private final PayStackPaymentService paymentService;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    public Long profileId(){
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Optional<Profile> profile = profileRepository.findByUserEmail(email);
        if (profile.isEmpty()) {
            throw new CustomNotFoundException("Student with Id " + profile.get().getUniqueRegistrationNumber() + " is not valid");
        }
        return profile.get().getId();

    }

    @Override
    public WalletResponse getProfileWalletBalance() {
        String email = SecurityConfig.getAuthenticatedUserEmail();

        Optional<Profile> profile = profileRepository.findByUserEmail(email);
        if (profile.isEmpty()) {
            throw new CustomNotFoundException("Student with Id " + profile.get().getUniqueRegistrationNumber() + " is not valid");
        }
        // Validate profile status
        AccountUtils.validateProfileStatus(profile.get());


        Wallet wallet = walletRepository.findWalletByUserProfile(profile.get())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found or phone number missing"));


        return new WalletResponse(wallet.getBalance(), wallet.getTotalMoneySent());

    }


    @Transactional
    public String fundWallet1(BigDecimal amount, Profile profile) {

        Wallet studentWallet = walletRepository.findWalletByUserProfile(profile)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        studentWallet.setBalance(studentWallet.getBalance().add(amount));
        walletRepository.save(studentWallet);

        return "balance updated successfully";

    }



//    @Transactional
//    public PaymentResponse fundWallet(FundWalletRequest fundWalletRequest) {
//        validateFundRequest(fundWalletRequest);
//
//        String email = fundWalletRequest.getEmail() != null ?
//                fundWalletRequest.getEmail() :
//                SecurityConfig.getAuthenticatedUserEmail();
//
//        Optional<Profile> profile = profileRepository.findByUserEmail(email);
//        if (profile.isEmpty()) {
//            throw new CustomNotFoundException("Student with Id " + profile.get().getUniqueRegistrationNumber() + " is not valid");
//        }
//
//        // Get the appropriate payment provider
//        PaymentProvider paymentProvider = paymentProviderFactory.getProvider(fundWalletRequest.getProvider());
//
//        PaymentRequestDto paymentRequest = new PaymentRequestDto(
//                email,
//                fundWalletRequest.getAmountAsBigDecimal(),
//                fundWalletRequest.getCallbackUrl(),
//                fundWalletRequest.getMetadata() != null ?
//                        fundWalletRequest.getMetadata() :
//                        createDefaultMetadata()
//        );
//
//        PaymentInitResponse paymentResponse = paymentProvider.initiatePayment(paymentRequest);
//
//        if (!paymentResponse.isStatus()) {
//            throw new PaymentProcessingException("Payment initialization failed: " + paymentResponse.getMessage());
//        }
//
//        return PaymentResponse.builder().authorizationUrl(paymentResponse.getAuthorizationUrl()).build();
//    }


    @Transactional
    public String transferFunds(TransferRequest transferRequest) {

        String email = SecurityConfig.getAuthenticatedUserEmail();

        Optional<Profile> senderProfile = profileRepository.findByUserEmail(email);
        if (senderProfile.isEmpty()) {
            throw new CustomNotFoundException("Student with Id " + senderProfile.get().getUniqueRegistrationNumber() + " is not valid");
        }

        Optional<Profile> receiverProfile = profileRepository.findByUniqueRegistrationNumber(transferRequest.getRegistrationNumber());
        if (receiverProfile.isEmpty()) {
            throw new CustomNotFoundException("Student with Id " + receiverProfile.get().getUniqueRegistrationNumber() + " is not valid");
        }
        Wallet senderWallet = senderProfile.get().getWallet();
        Wallet recipientWallet = receiverProfile.get().getWallet();


        // Perform the transfer
        senderWallet.transferTo(recipientWallet, transferRequest.getAmount());

        // Save both wallets
        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // Record transactions for both parties
        transactionService.recordTransaction(senderWallet, transferRequest.getAmount(), TransactionType.DEBIT, "Transfer to " + senderProfile.get().getUniqueRegistrationNumber()
        );
        transactionService.recordTransaction(recipientWallet, transferRequest.getAmount(), TransactionType.CREDIT, "Transfer from " + transferRequest.getRegistrationNumber());

        // Send notifications
        notificationService.sendTransferNotifications(senderWallet, recipientWallet, transferRequest.getAmount());

        return "Transfer to " + receiverProfile.get().getUniqueRegistrationNumber() + " successfully transferred";
    }

    @Override
    public void handlePaymentWebhook(WebhookRequest webhookRequest) {
        PaymentProvider provider = paymentProviderFactory.getProviderForWebhook(webhookRequest.getProvider());
        provider.handleWebhook(webhookRequest);
    }

    @Override
    @Transactional
    public void creditWalletFromWebhook(String reference, BigDecimal amount, String currency, String email, String gatewayResponse) {
        try {
            // 1. Validate inputs
            validateWebhookInputs(reference, amount, currency, email);

            // 2. Find the user profile
            Profile profile = profileRepository.findByUserEmail(email)
                    .orElseThrow(() -> new NotFoundException("Profile not found for email: " + email));

            // 3. Find or create wallet
            Wallet wallet = walletRepository.findWalletByUserProfile(profile)
                    .orElseGet(() -> createNewWallet(profile));

            // 4. Credit the wallet
            wallet.credit(amount);
            walletRepository.save(wallet);

            // 5. Record the transaction
            Transaction transaction = Transaction.builder()
                    .transactionType(TransactionType.CREDIT)
                    .user(profile)
                    .amount(amount)
                    .description("Wallet funding via webhook: " + gatewayResponse)
                    .status(TransacStatus.SUCCESS)
                    .paymentReference(reference)
                    .build();
            transactionRepository.save(transaction);

            // 6. Send notifications
            sendWebhookNotifications(profile, wallet, transaction, amount);

            logger.info("Successfully credited wallet via webhook. Reference: {}, Amount: {}, Email: {}",
                    reference, amount, email);

        } catch (Exception e) {
            logger.error("Failed to credit wallet via webhook. Reference: {}, Error: {}", reference, e.getMessage());
            throw new PaymentProcessingException("Failed to process webhook credit "+e);
        }
    }

    private void validateWebhookInputs(String reference, BigDecimal amount, String currency, String email) {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException("Reference cannot be null or empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || currency.isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }

    private void sendWebhookNotifications(Profile profile, Wallet wallet, Transaction transaction, BigDecimal amount) {
        try {
            // Create in-app notification
            createWebhookNotification(profile, transaction, amount);

            // Send email notification
            sendWebhookEmailNotification(profile.getUser(), wallet, amount);
        } catch (Exception e) {
            logger.error("Failed to send webhook notifications for transaction: {}", transaction.getPaymentReference(), e);
            // Swallow notification errors to avoid failing the entire transaction
        }
    }

    private void createWebhookNotification(Profile profile, Transaction transaction, BigDecimal amount) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        String message = String.format(
                "Your wallet was credited with ₦%s via webhook payment",
                formatter.format(amount)
        );

        Notification notification = Notification.builder()
                .notificationType(NotificationType.CREDIT_NOTIFICATION)
                .user(profile)
                .notificationStatus(NotificationStatus.UNREAD)
                .transaction(transaction)
                .message(message)
                .build();

        notificationRepository.save(notification);
    }

    private void sendWebhookEmailNotification(User user, Wallet wallet, BigDecimal amount) throws MessagingException {
        Map<String, Object> model = new HashMap<>();
        model.put("amount", amount);
        model.put("name", user.getFirstName() + " " + user.getLastName());
        model.put("balance", wallet.getBalance());
        model.put("source", "webhook payment");

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(user.getEmail())
                .subject("Wallet Credit Notification")
                .templateName("wallet-webhook-credit")
                .model(model)
                .build();

        emailService.sendHtmlEmail(emailDetails);
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
            throw new PaymentProcessingException("Payment processing error " + e);
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
    ) {
    }

    private record WalletOperationResult(
            User user,
            Profile profile,
            Wallet wallet,
            Transaction transaction,
            double amount
    ) {
    }


    public SchoolBalanceResponse schoolTotalWallet() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> admin = userRepository.findByEmail(email);
            if (admin.isEmpty()) {
                throw new CustomNotFoundException("Admin is not valid");
            }

            List<Wallet> wallets = walletRepository.findBySchoolAndWalletStatus(admin.get().getSchool(), WalletStatus.ACTIVE);
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
            throw new CustomInternalServerException("Error fetching school wallet balance" + e);
        }
    }

    private Map<String, Object> createDefaultMetadata() {
        return Map.of(
                "userId", profileId(),
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
