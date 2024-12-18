package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
import lombok.Data;

@Data
public class ClassSubjectRequest {
    private Long subjectId;
    private Long classBlockId;
    private Long academicYearId;
}
