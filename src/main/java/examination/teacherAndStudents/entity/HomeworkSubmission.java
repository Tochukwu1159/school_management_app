package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "homework_submission")
public class HomeworkSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Profile student;

    @Column(nullable = false)
    private String fileUrl; // Store submission file location

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private Double obtainedMark; // Nullable until teacher grades it

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
        this.status = SubmissionStatus.SUBMITTED;
    }
}
