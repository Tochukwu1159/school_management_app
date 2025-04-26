package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String week;

    private String description;  // You can add fields like description, syllabus, etc.

    private String resources;  // Optional field for resources associated with the curriculum (e.g., books, links)

    @ManyToOne
    @JoinColumn(name = "class_subject_id", nullable = false)
    private ClassSubject classSubject;  // Link Curriculum to ClassSubject

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassBlock classBlock;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Profile teacher;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm term;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @CreationTimestamp
    private LocalDateTime createdAt; // Automatically populate creation timestamp

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    }
