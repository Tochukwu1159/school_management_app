package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private Long assignedById;
    private String assignedByName;
    private Long assignedToId;
    private String assignedToName;
    private String description;
    private LocalDateTime dateAssigned;
    private String feedback;
    private String status;
}
