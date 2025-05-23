package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
import lombok.Data;

@Data
public class AttendancePercentageRequest {
    private long userId;
    private long classBlockId;
    private Long studentTermId;
    private Long sessionId;
}
