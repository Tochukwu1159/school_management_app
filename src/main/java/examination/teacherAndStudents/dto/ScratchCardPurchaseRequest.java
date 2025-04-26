package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScratchCardPurchaseRequest {
    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Term ID is required")
    private Long termId;

//    @NotNull(message = "Subclass ID is required")
//    private Long subClassId;
}