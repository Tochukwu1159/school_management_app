package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for ServiceOffered operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOfferedDTO {

    private Long id;

    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name cannot exceed 100 characters")
    private String name;

    private Boolean isDefault;
}