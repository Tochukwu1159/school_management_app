package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AssignmentResponse {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long subjectId;
    private String subjectName;
    private List<Long> classIds;       // List of class block IDs
    private List<String> classNames;   // List of class block names
    private String title;
    private String description;
    private String instructions;
    private String attachment;
    private int totalMark;
    private LocalDateTime dateIssued;
    private LocalDateTime dateDue;
}