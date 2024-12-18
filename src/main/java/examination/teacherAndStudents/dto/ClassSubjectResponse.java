package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.StudentTerm;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClassSubjectResponse {
    private Long id;
    private SubjectResponse subject;
    private ClassBlockResponses classBlock;
    private AcademicSessionResponse academicYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
