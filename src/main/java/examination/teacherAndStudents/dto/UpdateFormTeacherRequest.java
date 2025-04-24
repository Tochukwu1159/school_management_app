package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFormTeacherRequest {
    private Long sessionId;
    private Long classLevelId;
    private Long subclassId;
    private Long teacherId;
}
