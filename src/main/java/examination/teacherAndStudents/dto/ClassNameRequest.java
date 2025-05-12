package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassNameRequest {
    @NotNull
    @Size(min = 1, max = 100)
    private String name;
}