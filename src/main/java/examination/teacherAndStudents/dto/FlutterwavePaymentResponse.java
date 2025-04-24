package examination.teacherAndStudents.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import examination.teacherAndStudents.utils.PaymentMethod;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlutterwavePaymentResponse {
    private String status;
    private String message;
    private PaymentData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentData {
        private String link;

        @JsonProperty("tx_ref")
        private String transactionReference;

    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    public PaymentInitResponse toPaymentInitResponse() {
        return PaymentInitResponse.builder()
                .status(isSuccess())
                .message(message)
                .authorizationUrl(data != null ? data.getLink() : null)
                .reference(data != null ? data.getTransactionReference() : null)
                .paymentMethod(PaymentMethod.FLUTTERWAVE)
                .providerResponse(this.toString())
                .build();
    }
}