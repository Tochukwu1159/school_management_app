package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class CurriculumRequest { private String description;
    private Long termId;
    private String title;
    private String week;
    private String resources;  // Optional resources (e.g., books, materials)
}
