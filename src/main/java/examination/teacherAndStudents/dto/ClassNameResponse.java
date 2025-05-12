package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassNameResponse {
    private Long id;
    private String name;
}