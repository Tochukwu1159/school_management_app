package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
import lombok.Data;

import java.util.List;

@Data
public class ClassSubjectRequest {
    private List<Long> subjectIds;
    private Long classBlockId;
    private Long academicYearId;
}
