package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class ClassLevelRequest {
    @NotNull(message = "Class name ID is required")
    private Long classNameId;
    private List<@NotBlank(message = "Block name cannot be blank") @Pattern(regexp = "^[a-zA-Z0-9-]{1,50}$", message = "Invalid block name format") String> classBlocks;
}