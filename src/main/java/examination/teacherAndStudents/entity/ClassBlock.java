package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.StudentTerm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "class_block") // Renamed for better clarity
@Entity
@Builder
public class ClassBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String currentStudentClassName;  // More descriptive name for the class section

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_level_id", nullable = false)
    private ClassLevel classLevel; // Relates to the class level


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Profile formTeacher;

    @Column(nullable = false, unique = true, length = 255)
    private String classUniqueUrl;

    @Column(columnDefinition = "int default 0")
    private int numberOfStudents;

    @OneToMany(mappedBy = "classBlock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Profile> studentList; // Many profiles belong to a class block

    @OneToMany(mappedBy = "classBlock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassSubject> subjects;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Timestamp for updates

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }// Timestamp for the class block, could be adjusted to suit needs


}
