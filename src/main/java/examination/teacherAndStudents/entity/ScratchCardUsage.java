package examination.teacherAndStudents.entity;


import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "scratch_card_usages")
@Entity
@Builder
public class ScratchCardUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "scratch_card_id", nullable = false)
    private ScratchCard scratchCard;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile student;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false)
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        this.usedAt = LocalDateTime.now();
    }
}