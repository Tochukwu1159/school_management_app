package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreRequest {
    private long classLevelId;
    private long classBlockId;
    private  Long studentId;
    private Long sessionId;
    private Long subjectId;
    @Max(70)
    private int examScore;
    @Max(30)
    private int assessmentScore;
    private Long termId;
}
