package examination.teacherAndStudents.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "promotion_criteria")
@Entity
@Builder
public class PromotionCriteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_block_id", nullable = false)
    private ClassBlock classBlock; // Current class

    @Column(nullable = false)
    private int cutOffScore; // e.g., 80

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "future_session_id", nullable = false)
    private AcademicSession futureSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promoted_class_id", nullable = false)
    private ClassBlock promotedClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_session_id", nullable = false)
    private AcademicSession currentSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demoted_class_id", nullable = false)
    private ClassBlock demotedClassId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
