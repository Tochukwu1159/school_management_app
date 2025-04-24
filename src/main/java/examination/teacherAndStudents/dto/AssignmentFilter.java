package examination.teacherAndStudents.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssignmentFilter {
    private Long teacherId;
    private Long subjectId;
    private String title;
    private LocalDateTime dateIssuedFrom;
    private LocalDateTime dateIssuedTo;
    private LocalDateTime dateDueFrom;
    private LocalDateTime dateDueTo;
    private Long classBlockId;
}
