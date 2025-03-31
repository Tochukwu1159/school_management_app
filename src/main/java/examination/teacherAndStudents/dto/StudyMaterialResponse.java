package examination.teacherAndStudents.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyMaterialResponse {
    private Long id;
    private String title;
    private String filePath;
    private String subjectName;
    private String teacherName;
    private String academicYear;
    private String term;
}
