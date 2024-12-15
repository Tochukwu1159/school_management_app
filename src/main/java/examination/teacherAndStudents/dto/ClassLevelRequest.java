package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class ClassLevelRequest {
    private String className;
    private Long academicSessionId;

}
