package examination.teacherAndStudents.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequest {
    private Long assignedById;    // ID of the person assigning the task (school owner or teacher)
    private Long assignedToId;    // ID of the person receiving the task (teacher, driver, etc.)
    private String description;   // Task description
    private LocalDateTime dateAssigned;  // Date when the task is assigned
    private String feedback;      // Feedback for the task once completed
    private String status;        // Task status (e.g., "pending", "in-progress", "done")
}
