package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private Profile assignedBy;  // Teacher or school owner assigning the task

    @ManyToOne
    @JoinColumn(name = "assigned_to_id", nullable = false)
    private Profile assignedTo;  // The person receiving the task (teacher, driver, gatekeeper, etc.)

    private String description;  // Task description

    private LocalDateTime dateAssigned;  // Date when the task was assigned
    private String feedback;  // Feedback from the assignee after task completion
    private String status;  // Status of the task (e.g., "pending", "in-progress", "done")

}
