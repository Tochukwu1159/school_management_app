package examination.teacherAndStudents.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlutterwaveVerificationResponse {
    private String status;
    private String message;
    private VerificationData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificationData {
        private String id;

        @JsonProperty("tx_ref")
        private String transactionReference;

        @JsonProperty("flw_ref")
        private String flutterwaveReference;

        private BigDecimal amount;
        private String currency;
        private String status;

        @JsonProperty("payment_type")
        private String paymentType;

        @JsonProperty("customer")
        private Customer customer;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customer {
        private String email;
        private String name;
    }

    public PaymentVerificationResponse toStandardResponse() {
        return PaymentVerificationResponse.builder()
                .success("successful".equalsIgnoreCase(status))
                .transactionId(data != null ? data.getId() : null)
                .reference(data != null ? data.getTransactionReference() : null)
                .paymentMethod(data != null ? data.getPaymentType() : null)
                .amount(data != null ? data.getAmount() : null)
                .currency(data != null ? data.getCurrency() : null)
                .paymentDate(data != null ? data.getCreatedAt() : null)
                .customerEmail(data != null && data.getCustomer() != null ?
                        data.getCustomer().getEmail() : null)
                .statusMessage(message)
                .provider("flutterwave")
                .build();
    }
}