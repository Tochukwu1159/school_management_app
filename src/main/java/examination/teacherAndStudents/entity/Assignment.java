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
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Profile teacher;  // Assigning teacher to the assignment

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Profile student;  // Student profile (or recipient)

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;  // Subject to which the assignment belongs

    private String description;  // Assignment description
    private String attachment;   // Link to the attachment (could be a file URL or filename)

    private LocalDateTime dateIssued;  // Date when the assignment was issued
    private LocalDateTime dateDue;     // Date when the assignment is due

}
