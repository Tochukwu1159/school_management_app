package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurriculumResponse {
    private Long id;
    private String title;
    private Long classSubjectId;
    private String description;
    private String resources;  // Optional
    private Long subjectId;
    private String subjectName;
}
