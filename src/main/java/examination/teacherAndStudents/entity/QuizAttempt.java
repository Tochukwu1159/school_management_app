package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Profile student;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ElementCollection
    @CollectionTable(name = "quiz_attempt_questions", joinColumns = @JoinColumn(name = "attempt_id"))
    private List<AssignedQuestion> assignedQuestions;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime submissionDeadline;

    @Column(nullable = false)
    private boolean completed = false;

    @Data
    @Embeddable
    public static class AssignedQuestion {
        private String questionId;
        private String questionText;
        @Column(columnDefinition = "TEXT")
        private String options;
    }
}