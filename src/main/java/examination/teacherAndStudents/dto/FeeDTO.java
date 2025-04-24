package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a fee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeDTO {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private boolean isCompulsory;

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    private Long classLevelId;

    private Long subClassId;

    private Long termId;
}