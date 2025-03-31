package examination.teacherAndStudents.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
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

    @Column(nullable = false)
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        this.usedAt = LocalDateTime.now();
    }
}