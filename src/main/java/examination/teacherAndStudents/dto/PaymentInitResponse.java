package examination.teacherAndStudents.dto;
import examination.teacherAndStudents.utils.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentInitResponse {
    @Getter
    private String id;
    private boolean status;
    private String message;
    private String authorizationUrl;
    private String reference;
    private PaymentMethod paymentMethod;
    private String providerResponse; // Raw response from payment provider
    private LocalDateTime expiresAt; // When the payment link expires

    public static PaymentInitResponse failed(String message) {
        return PaymentInitResponse.builder()
                .status(false)
                .message(message)
                .build();
    }
}