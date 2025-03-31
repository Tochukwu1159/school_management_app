package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PromotionCriteriaRequest {
    @NotNull(message = "Current class block ID is required")
    private Long classBlockId;

    @NotNull(message = "Current session ID is required")
    private Long currentSessionId;

    @NotNull(message = "Future session ID is required")
    private Long futureSessionId;

    @NotNull(message = "Promoted class ID is required")
    private Long promotedClassId;

    @NotNull(message = "Demoted class ID is required")
    private Long demotedClassId;

    @Min(value = 0, message = "Cut-off score must be at least 0")
    @Max(value = 100, message = "Cut-off score must be at most 100")
    private int cutOffScore;
}