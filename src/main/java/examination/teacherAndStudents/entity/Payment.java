package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.FeeStatus;
import examination.teacherAndStudents.utils.PaymentMethod;
import examination.teacherAndStudents.utils.Purpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Setter
@Builder
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    private String provider;

    @Enumerated
    private FeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(length = 50)
    private String referenceNumber;

    @Column(length = 100)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_fee_id")
    private Fee studentFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AcademicSession academicSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private StudentTerm studentTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_level_id")
    private ClassLevel classLevel;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_block_id")
    private ClassBlock classBlock;

   private boolean paid=false;

   private boolean fullyPaid=false;

@Enumerated()
    private Purpose purpose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_category_id", nullable = false)
    private FeeCategory feeCategory;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Getters and setters
}