package examination.teacherAndStudents.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizSubmissionResponse {
    private Long resultId;
    private Integer score;
    private List<FeedbackDTO> feedback;

    @Data
    public static class FeedbackDTO {
        private String questionId;
        private Boolean correct;
        private String explanation;
    }
}