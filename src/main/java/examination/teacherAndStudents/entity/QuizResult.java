package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "quiz_results")
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Profile student;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer total;

    @ElementCollection
    @CollectionTable(name = "quiz_result_answers", joinColumns = @JoinColumn(name = "result_id"))
    private List<Answer> answers;

    @ElementCollection
    @CollectionTable(name = "quiz_result_feedback", joinColumns = @JoinColumn(name = "result_id"))
    private List<Feedback> feedback;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    @Embeddable
    public static class Answer {
        private String questionId;
        private Integer selectedOption;
    }

    @Data
    @Embeddable
    public static class Feedback {
        private String questionId;
        private Boolean correct;
        private String explanation;
    }
}