package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.Year;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lessonNote")
@Entity
@Builder
public class LessonNote{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lessonPlanId", nullable = false)
    private LessonPlan lessonPlan;

    private String topic;

    @Lob
    private String keyPoints;

    @Lob
    private String functions;

    private String title;
    private String content;

    private String diagram;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;

    @ManyToOne
    private User teacher;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
