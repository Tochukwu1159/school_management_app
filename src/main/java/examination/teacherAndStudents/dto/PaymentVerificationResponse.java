package examination.teacherAndStudents.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationResponse {
    private boolean success;
    private String transactionId;
    private String reference;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paymentDate;
    private String customerEmail;
    private String statusMessage;
    private String provider;

    // Additional metadata if needed
    private Object metadata;

    public static PaymentVerificationResponse failed(String message) {
        return PaymentVerificationResponse.builder()
                .success(false)
                .statusMessage(message)
                .build();
    }
}