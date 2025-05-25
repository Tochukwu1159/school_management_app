package examination.teacherAndStudents.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizSubmissionRequest {
    private Long quizId;
    private List<AnswerDTO> answers;

    @Data
    public static class AnswerDTO {
        private String questionId;
        private Integer selectedOption;
    }
}