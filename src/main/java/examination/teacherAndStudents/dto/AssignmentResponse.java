package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponse {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long profileId;
    private String profileName;
    private Long subjectId;
    private String subjectName;
    private String description;
    private String attachment;
    private LocalDateTime dateIssued;
    private LocalDateTime dateDue;
}
