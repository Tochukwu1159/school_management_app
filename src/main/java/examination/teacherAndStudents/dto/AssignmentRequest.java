package examination.teacherAndStudents.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssignmentRequest {
    private Long teacherId;     // Teacher issuing the assignment
    private Long profileId;     // Student profile to whom the assignment is given
    private Long subjectId;     // Subject for the assignment
    private String description; // Assignment description
    private String attachment;  // Assignment attachment
    private LocalDateTime dateIssued; // Date the assignment is issued
    private LocalDateTime dateDue;    // Date the assignment is due
    private int totalMark;
    private List<Long> classIds;
    private String instructions;
    private String title;

}
