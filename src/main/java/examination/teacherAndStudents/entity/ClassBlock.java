package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "class_block")
@Entity
@Builder
public class ClassBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Block name is required")
    @Size(max = 100, message = "Block name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_level_id", nullable = false)
    @ToString.Exclude
    private ClassLevel classLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @ToString.Exclude
    private Profile formTeacher;

    @Pattern(regexp = "^[a-zA-Z0-9-]{1,255}$", message = "Invalid URL format")
    @Column(unique = true, length = 255)
    private String classUniqueUrl;

    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int numberOfStudents;

    @OneToMany(mappedBy = "classBlock", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Profile> studentList = new ArrayList<>();

    @OneToMany(mappedBy = "classBlock", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ClassSubject> subjects = new ArrayList<>();

    @ManyToMany(mappedBy = "classBlocks")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Assignment> assignments = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addAssignment(Assignment assignment) {
        if (assignment != null && !assignments.contains(assignment)) {
            assignments.add(assignment);
            assignment.getClassBlocks().add(this);
        }
    }

    public void removeAssignment(Assignment assignment) {
        if (assignment != null && assignments.contains(assignment)) {
            assignments.remove(assignment);
            assignment.getClassBlocks().remove(this);
        }
    }
}