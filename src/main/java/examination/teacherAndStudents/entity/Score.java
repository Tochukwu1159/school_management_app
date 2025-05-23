package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.Session;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "score")
@Entity
@Builder
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(0)
    @Max(100)
    private int examScore;

    @Min(0)
    @Max(100)
    private int assessmentScore;

    @NotBlank
    private String subjectName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Profile userProfile;

    @ManyToOne
    @JoinColumn(name = "sessionClass_id", nullable = false)
    private SessionClass sessionClass;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private ClassSubject subject;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;





    // Constructors, getters, and setters
}
