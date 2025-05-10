package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportPaymentRequest {
    @NotNull(message = "Fee ID is required")
    private Long feeId;

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    @NotNull(message = "Stop  is required")
    private String stop;
}