package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "assignments")
public class
Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Profile teacher;  // Assigning teacher to the assignment

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;  // Subject to which the assignment belongs

    private int totalMark = 0;

    private String instructions;
    private String title;


    @ManyToMany
    @JoinTable(
            name = "assignment_classes",
            joinColumns = @JoinColumn(name = "assignment_id"),
            inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    private Set<ClassBlock> classBlocks = new HashSet<>(); // Initialize to empty set

    // Helper methods for managing class blocks
    public void addClassBlock(ClassBlock classBlock) {
        this.classBlocks.add(classBlock);
        classBlock.getAssignments().add(this);
    }

    public void removeClassBlock(ClassBlock classBlock) {
        this.classBlocks.remove(classBlock);
        classBlock.getAssignments().remove(this);
    }

    private String description;  // Assignment description
    private String attachment;   // Link to the attachment (could be a file URL or filename)

    private LocalDateTime dateIssued;  // Date when the assignment was issued
    private LocalDateTime dateDue;     // Date when the assignment is due

}
