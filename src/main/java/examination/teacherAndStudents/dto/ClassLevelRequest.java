package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class ClassLevelRequest {
    @NotBlank(message = "Class name is required")
    private String className;

    @NotNull(message = "Academic session ID is required") // Changed from @NotEmpty
    private Long academicSessionId;

    @NotEmpty(message = "At least one class block is required")
    private List<@NotBlank(message = "Block name cannot be blank") @Pattern(regexp = "^[a-zA-Z0-9-]{1,50}$", message = "Invalid block name format") String> classBlocks;
}