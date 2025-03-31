package examination.teacherAndStudents.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "scratch_cards")
@Entity
@Builder
public class ScratchCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cardNumber; // Generated unique number

    @Column(nullable = false)
    private String pin; // Secret PIN (should be encrypted)

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer maxUsageCount = 5;

    @Column(nullable = false)
    private Integer currentUsageCount = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private LocalDateTime assignedAt;

    @OneToMany(mappedBy = "scratchCard", cascade = CascadeType.ALL)
    private List<ScratchCardUsage> usages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Profile student;


    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne
    @JoinColumn(name = "accademic_session_id", nullable = false)
    private AcademicSession academicSession;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plusYears(1); // 1 year validity
    }


}