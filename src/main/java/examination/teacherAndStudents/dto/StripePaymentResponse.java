package examination.teacherAndStudents.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentResponse {
    private String id;
    private String object;
    private String status;
    private String paymentIntent;

    @JsonProperty("url")
    private String checkoutUrl;

    @JsonProperty("amount_total")
    private Long amountTotal;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("expires_at")
    private Instant expiresAt;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("success_url")
    private String successUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;

    // Helper methods
    public boolean isSuccess() {
        return "complete".equalsIgnoreCase(status) ||
                "succeeded".equalsIgnoreCase(paymentStatus);
    }

    public BigDecimal getAmountInNaira() {
        return BigDecimal.valueOf(amountTotal).divide(BigDecimal.valueOf(100));
    }
}