package examination.teacherAndStudents.dto;


import lombok.Data;
import java.util.List;

@Data
public class QuizCreationResponse {
    private Long quizId;
    private String title;
    private Long subjectId;
    private List<QuestionDTO> questions;

    @Data
    public static class QuestionDTO {
        private String question;
        private List<String> options;
        private String correctAnswer;
        private String explanation;
    }
}
