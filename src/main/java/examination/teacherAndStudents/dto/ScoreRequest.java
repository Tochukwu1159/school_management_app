package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
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
    private  Long studentId;
    private Long sessionId;
    private Long subjectId;
    private int examScore;
    private int assessmentScore;
    private Long termId;
}
