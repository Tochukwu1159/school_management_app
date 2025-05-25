package examination.teacherAndStudents.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizQuestionsResponse {
    private Long quizId;
    private String title;
    private Long subjectId;
    private List<QuestionDTO> questions;
    private LocalDateTime submissionDeadline;

    @Data
    public static class QuestionDTO {
        private String questionId;
        private String questionText;
        private List<String> options;
    }
}