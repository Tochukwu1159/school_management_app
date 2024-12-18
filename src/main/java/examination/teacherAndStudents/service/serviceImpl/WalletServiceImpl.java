package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.PayStackPaymentService;
import examination.teacherAndStudents.service.WalletService;
import examination.teacherAndStudents.utils.NotificationStatus;
import examination.teacherAndStudents.utils.NotificationType;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.TransactionType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final EmailService emailService;

    private final PayStackPaymentService paymentService;
    //    private final TransactionDao transactionDao;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;

    @Override
    public WalletResponse getStudentWalletBalance() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Optional<User> student = userRepository.findByEmail(email);
        if (student == null) {
            throw new CustomNotFoundException("Student with Id " + student.get().getId() + " is not valid");
        }
        Optional<Profile> studentProfile = profileRepository.findByUser(student.get());
        if (studentProfile.isEmpty()) {
            throw new CustomNotFoundException("Student with Id " + student.get().getId() + " is not valid");
        }

        Wallet wallet = walletRepository.findWalletByUserProfile(studentProfile.get());
        if (wallet == null) {
            throw new CustomNotFoundException("Wallet not found or phone number missing");
        }


        return new WalletResponse(wallet.getBalance(), wallet.getTotalMoneySent());

    }

    @Override
    public PaymentResponse fundWallet(FundWalletRequest fundWalletRequest) throws Exception {
        try {
            fundWalletRequest.setAmount(String.valueOf(Integer.parseInt(fundWalletRequest.getAmount()) * 100));
            String email = SecurityConfig.getAuthenticatedUserEmail();

            PayStackTransactionRequest payStackTransactionRequest = PayStackTransactionRequest.builder()
                    .email(email)
                    .amount(new BigDecimal(fundWalletRequest.getAmount()))
                    .build();
//            PayStackTransactionResponse transactionResponse = new PayStackTransactionResponse();

            PayStackTransactionResponse transactionResponse = paymentService.initTransaction(payStackTransactionRequest);
//            transactionResponse.setStatus(true);  // Default value for status (false)
//            transactionResponse.setMessage("Default message");  // Default message
//            transactionResponse.setStatusCode(0);

            if (!transactionResponse.isStatus()) {
                throw new Exception("Payment not authorized");
            }

            Optional<User> user = userRepository.findByEmail(email);
            User student = user.get();

            if (student == null) {
                throw new CustomNotFoundException("User with email " + email + " is not valid");
            }

            Optional<Profile> studentProfile = profileRepository.findByUser(student);

            if (studentProfile.isEmpty()) {
                throw new CustomNotFoundException("Student with email " + email + " is not valid");
            }


            fundWalletRequest.setAmount(String.valueOf(Integer.parseInt(fundWalletRequest.getAmount()) / 100));
            double amount = Double.parseDouble(fundWalletRequest.getAmount());
            DecimalFormat formatter = new DecimalFormat("#,###.00");

            Wallet wallet = walletRepository.findWalletByUserProfile(studentProfile.get());

            if (wallet == null) {
                createNewWalletAndTransaction(student, fundWalletRequest.getAmount(), transactionResponse, formatter);
                sendWalletFundingEmail(student, fundWalletRequest.getAmount());
                return new PaymentResponse("Success");
            }

            updateExistingWalletAndTransaction(student, fundWalletRequest.getAmount(), transactionResponse, formatter, wallet);
            sendWalletFundingEmail(student, fundWalletRequest.getAmount());

            return new PaymentResponse(transactionResponse.getData().getAuthorization_url());
        } catch (Exception ex) {
            ex.printStackTrace(); // Log the exception for debugging
            throw new Exception("Failure funding wallet");
        }
    }

    private void createNewWalletAndTransaction(User student, String amount, PayStackTransactionResponse transactionResponse, DecimalFormat formatter) {
        Optional<Profile> user = profileRepository.findByUser(student);

        if (user == null) {
            throw new CustomNotFoundException("Student profile does not exist");
        }
        Wallet walletDao1 = Wallet.builder()
                .balance(new BigDecimal(amount))
                .userProfile(user.get())
                .build();
        walletRepository.save(walletDao1);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.CREDIT)
                .user(user.get())
                .amount(new BigDecimal(amount))
                .description(transactionResponse.getMessage())
                .build();
        transactionRepository.save(transaction);

        Notification notification = Notification.builder()
                .notificationType(NotificationType.CREDIT_NOTIFICATION)
                .user(user.get())
                .notificationStatus(NotificationStatus.UNREAD)
                .transaction(transaction)
                .message("You funded your wallet with ₦" + formatter.format(Double.parseDouble(amount)))
                .build();
        notificationRepository.save(notification);
    }

    private void updateExistingWalletAndTransaction(User student, String amount, PayStackTransactionResponse transactionResponse, DecimalFormat formatter, Wallet wallet) {
        Optional<Profile> user = profileRepository.findByUser(student);


        if (user == null) {
            throw new CustomNotFoundException("Student profile does not exist");
        }
        BigDecimal result = wallet.getBalance().add(new BigDecimal(amount));
        wallet.setBalance(result);
        wallet.setUserProfile(user.get());
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.CREDIT)
                .user(user.get())
                .amount(new BigDecimal(amount))
                .description(transactionResponse.getMessage())
                .build();
        transactionRepository.save(transaction);

        Notification notification = Notification.builder()
                .notificationType(NotificationType.CREDIT_NOTIFICATION)
                .user(user.get())
                .notificationStatus(NotificationStatus.UNREAD)
                .transaction(transaction)
                .message("You funded your wallet with ₦" + formatter.format(Double.parseDouble(amount)))
                .build();
        notificationRepository.save(notification);
    }

    private void sendWalletFundingEmail(User student, String amount) {
        Map<String, Object> model = new HashMap<>();
        model.put("amount", amount);
        model.put("name", student.getFirstName() + " " + student.getLastName());
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(student.getEmail())
                .subject("Wallet funding status")
                .templateName("email-template-wallet")
                .model(model)
                .build();
        emailService.sendEmails(emailDetails);
    }

    public SchoolBalanceResponse schoolTotalWallet() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> admin = userRepository.findByEmail(email);
            if (!admin.isPresent()) {
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
