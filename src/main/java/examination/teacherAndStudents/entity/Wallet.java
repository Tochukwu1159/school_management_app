package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.error_handler.InsufficientBalanceException;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMoneyReceived = BigDecimal.ZERO;

    private LocalDateTime lastTransactionTime;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private Profile userProfile;
//    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)


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
    public synchronized void transferTo(Wallet recipient, BigDecimal amount) {
        this.debit(amount); // Will throw if insufficient funds
        recipient.credit(amount);
    }
}
