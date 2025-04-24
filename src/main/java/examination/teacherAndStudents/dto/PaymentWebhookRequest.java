package examination.teacherAndStudents.dto;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.utils.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentWebhookRequest {
    @NotBlank
    private String reference;

    @NotNull
    private PaymentMethod paymentMethod;

    private BigDecimal amount;
    private String currency;
    private String customerEmail;
    private String status;
    private LocalDateTime paidAt;
    private Map<String, Object> metadata;
    private String rawPayload; // Original payload from payment provider

    public boolean isSuccessful() {
        return "success".equalsIgnoreCase(status) ||
                "successful".equalsIgnoreCase(status) ||
                "completed".equalsIgnoreCase(status);
    }
}
