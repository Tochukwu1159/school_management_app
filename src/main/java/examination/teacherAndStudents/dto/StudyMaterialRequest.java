package examination.teacherAndStudents.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyMaterialRequest {
    private String title;
    private String filePath;
    private Long subjectId;
    private Long teacherId;
    private Long academicYearId;
    private Long termId;
}
