package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.StudentTerm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lessonNote")
@Entity
@Builder
public class LessonNote{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private Year year;

    @Enumerated(EnumType.STRING)
    private StudentTerm term;

    @ManyToOne
    private User teacher;
}
