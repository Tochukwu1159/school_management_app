package examination.teacherAndStudents.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizResultsResponse {
    private Long resultId;
    private Long userId;
    private Integer score;
    private List<AnswerDTO> answers;
    private List<FeedbackDTO> feedback;

    @Data
    public static class AnswerDTO {
        private String questionId;
        private Integer selectedOption;
    }

    @Data
    public static class FeedbackDTO {
        private String questionId;
        private Boolean correct;
        private String explanation;
    }
}