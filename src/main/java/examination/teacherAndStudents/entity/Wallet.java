package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.math.BigDecimal;


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

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private Profile userProfile;
//    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
}
