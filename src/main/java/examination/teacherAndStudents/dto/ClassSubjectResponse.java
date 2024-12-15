package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.StudentTerm;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassSubjectResponse {
    private Long id;
    private Subject subject;
    private ClassBlock classBlock;
    private StudentTerm term;
    private AcademicSession academicYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
