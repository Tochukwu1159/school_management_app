package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paystack_subaccount_code")
    private String paystackSubaccountCode;

    @Column(name = "flutterwave_linked_account_id")
    private String flutterwaveLinkedAccountId;
}