package examination.teacherAndStudents.dto;



import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GenerateScratchCardsRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Must generate at least 1 card")
    @Max(value = 5000, message = "Cannot generate more than 5000 cards at once")
    private int quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "1", message = "Price must be at least 0.01")
    @DecimalMax(value = "10000", message = "Price cannot exceed 10,000")
    private BigDecimal price;

    @NotNull(message = "Academic session ID is required")
    @Positive(message = "Invalid session ID")
    private Long sessionId;

    @NotNull(message = "Term ID is required")
    @Positive(message = "Invalid term ID")
    private Long termId;

    @NotNull(message = "Max usage count is required")
    @Min(value = 1, message = "Minimum usage count is 1")
    @Max(value = 20, message = "Maximum usage count is 20")
    private int maxUsageCount = 5;

    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiration;
}