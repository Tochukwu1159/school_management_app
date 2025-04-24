package examination.teacherAndStudents.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeVerificationResponse {
    private String id;
    private String object;
    private String status;

    @JsonProperty("amount_total")
    private Long amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("customer_details")
    private CustomerDetails customerDetails;

    @JsonProperty("payment_intent")
    private String paymentIntent;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDetails {
        private String email;
        private String name;
    }

    // Conversion method to standard verification response
    public PaymentVerificationResponse toStandardResponse() {
        return PaymentVerificationResponse.builder()
                .success("paid".equalsIgnoreCase(paymentStatus))
                .transactionId(id)
                .reference(paymentIntent)
                .paymentMethod("card") // Stripe primarily handles cards
                .amount(BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100)))
                .currency(currency)
                .paymentDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .customerEmail(customerDetails != null ? customerDetails.getEmail() : null)
                .statusMessage(status)
                .provider("stripe")
                .build();
    }
}