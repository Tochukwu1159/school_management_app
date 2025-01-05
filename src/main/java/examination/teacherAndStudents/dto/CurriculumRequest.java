package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class CurriculumRequest {
    private Long subjectId;  // Link to Subject
    private String description;
    private String resources;  // Optional resources (e.g., books, materials)
}
