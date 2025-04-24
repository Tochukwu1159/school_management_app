package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.error_handler.InsufficientBalanceException;
import examination.teacherAndStudents.utils.WalletStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @DecimalMin(value = "0.00", inclusive = true,  message = "Balance cannot be negative.") // Allow zero values
    @Digits(integer = 9, fraction = 2, message = "Invalid balance format.")
    @Column(name = "balance", nullable = false, columnDefinition = "NUMERIC(11,2) DEFAULT 0.0")
    private BigDecimal balance;

    @Column(name = "total_money_sent", nullable = false, columnDefinition = "NUMERIC(11,2) DEFAULT 0.0")
    @DecimalMin(value = "0.00", inclusive = true, message = "Total money sent cannot be negative.")
    @Digits(integer = 9, fraction = 2, message = "Invalid money sent format.")
    private BigDecimal totalMoneySent;

    @Enumerated(EnumType.STRING)
    private WalletStatus walletStatus;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMoneyReceived = BigDecimal.ZERO;

    private LocalDateTime lastTransactionTime;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;


    @OneToOne
    @JoinColumn(name = "profile_id")
    private Profile userProfile;

    /**
     * Credits amount to the wallet with validation and audit trail
     * @param amount Positive decimal amount to credit
     * @throws IllegalArgumentException if amount is not positive
     */
    public synchronized void credit(BigDecimal amount) {
        // Validation
        if (amount == null) {
            throw new IllegalArgumentException("Credit amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    String.format("Credit amount must be positive. Provided: %s", amount));
        }

        // Atomic operation
        this.balance = this.balance.add(amount);
        this.totalMoneyReceived = this.totalMoneyReceived.add(amount);
        this.lastTransactionTime = LocalDateTime.now();

    }

    /**
     * Debits amount from the wallet with validation
     * @param amount Positive decimal amount to debit
     * @throws IllegalArgumentException if amount is not positive
     * @throws InsufficientBalanceException if balance is insufficient
     */
    public synchronized void debit(BigDecimal amount) {
        // Validation
        if (amount == null) {
            throw new IllegalArgumentException("Debit amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    String.format("Debit amount must be positive. Provided: %s", amount));
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: %s, Required: %s",
                            this.balance, amount));
        }

        // Atomic operation
        this.balance = this.balance.subtract(amount);
        this.totalMoneySent = this.totalMoneySent.add(amount);
        this.lastTransactionTime = LocalDateTime.now();
    }

    // Additional business methods
    /**
     * Transfers money to another wallet within the same school
     * @param recipientWallet The wallet to receive the funds
     * @param amount The amount to transfer
     * @throws IllegalArgumentException if wallets are not in the same school or invalid amount
     * @throws InsufficientBalanceException if sender has insufficient funds
     */
    public synchronized void transferTo(Wallet recipientWallet, BigDecimal amount) {
        // Basic validations
        if (recipientWallet == null) {
            throw new IllegalArgumentException("Recipient wallet cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Transfer amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        // Check if wallets belong to the same school
        if (!this.userProfile.getUser().getSchool().getId()
                .equals(recipientWallet.getUserProfile().getUser().getSchool().getId())) {
            throw new IllegalArgumentException("Can only transfer to wallets within the same school");
        }

        // Check if trying to transfer to self
        if (this.id.equals(recipientWallet.getId())) {
            throw new IllegalArgumentException("Cannot transfer to your own wallet");
        }

        // Perform the transfer
        this.debit(amount);
        recipientWallet.credit(amount);
    }
}
