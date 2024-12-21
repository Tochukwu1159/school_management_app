package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
import lombok.Data;

@Data
public class AttendancePercentageRequest {
    private long userId;
    private long classLevelId;
    private Long studentTermId;
    private Long sessionId;
}
