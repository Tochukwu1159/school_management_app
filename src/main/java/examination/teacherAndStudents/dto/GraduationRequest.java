package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraduationRequest {
    @NotNull(message = "Academic session ID is required")
    private Long academicSessionId;

    @NotEmpty(message = "At least one class block ID is required")
    private List<Long> classBlockIds;
}