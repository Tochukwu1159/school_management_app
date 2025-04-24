package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for fee category operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeCategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}