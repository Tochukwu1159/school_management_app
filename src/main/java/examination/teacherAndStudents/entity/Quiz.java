package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Profile teacher;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private ClassSubject subject;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int questionsPerStudent;

    @Column
    private String pdfUrl;

    @ElementCollection
    @CollectionTable(name = "quiz_questions", joinColumns = @JoinColumn(name = "quiz_id"))
    private List<Question> questions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status = QuizStatus.UPLOADED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime quizTime; // New field for quiz expiration time

    @Column
    private Integer duration;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    public enum QuizStatus {
        UPLOADED, GENERATED, GENERATED_DIFFICULTY
    }

    @Data
    @Embeddable
    public static class Question {

        @Column(name = "question_id")
        private String id = java.util.UUID.randomUUID().toString();
        @Column(nullable = false)
        private String questionText;

        @Column(columnDefinition = "TEXT") // Use TEXT to store JSON string
        private String options;

        @Column(nullable = false)
        private Integer correctOption;

        @Column
        private String explanation;
    }
}