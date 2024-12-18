package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.databind.annotation.EnumNaming;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StudyMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String filePath; // Path to the stored PDF file
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Subject subject; // Link to the associated Course entity
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher; // Link to the teacher (assumed to be a User entity)
    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;
}