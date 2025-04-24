package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.TransactionResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Transaction;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.entity.Wallet;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.TransactionRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.repository.WalletRepository;
import examination.teacherAndStudents.service.TransactionService;
import examination.teacherAndStudents.utils.AccountUtils;
import examination.teacherAndStudents.utils.TransacStatus;
import examination.teacherAndStudents.utils.TransactionType;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ProfileRepository profileRepository;
    private final WalletRepository walletRepository;


    @Override
    public List<TransactionResponse> getProfileTransactions(int offset, int pageSize) throws Exception {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User student = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("Profile with email " + email + " is not valid"));

            Profile profile = profileRepository.findByUser(student)
                    .orElseThrow(() -> new NotFoundException("Profile not found"));


            Pageable pageable = PageRequest.of(offset, pageSize);
            Page<Transaction> transactions = transactionRepository.findTransactionByUserOrderByCreatedAtDesc(pageable, profile);

            return transactions.stream()
                    .map(transaction -> TransactionResponse.builder()
                            .transactionType(transaction.getTransactionType().name())
                            .amount(transaction.getAmount())
                            .description(transaction.getDescription())
                            .createdAt(AccountUtils.localDateTimeConverter(transaction.getCreatedAt()))
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            throw new CustomInternalServerException("Error retrieving student transactions");
        }
    }

    @Transactional
    public void recordTransaction(Wallet wallet, BigDecimal amount, TransactionType transactionType, String description) {
        // Validate inputs
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        // Create and save the transaction
        Transaction transaction = Transaction.builder()
                .user(wallet.getUserProfile())
                .amount(amount)
                .transactionType(transactionType)
                .status(TransacStatus.SUCCESS)
                .description(description)
                .paymentReference(generateTransactionReference())
                .build();

        transactionRepository.save(transaction);

        // Update wallet's last transaction time
        wallet.setLastTransactionTime(LocalDateTime.now());
        walletRepository.save(wallet);
    }

    private String generateTransactionReference() {
        return "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}



