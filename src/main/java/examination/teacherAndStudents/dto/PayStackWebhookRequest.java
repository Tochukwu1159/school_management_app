package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayStackWebhookRequest {
    private String event;
    private PayStackWebhookData data;

    @Data
    public static class PayStackWebhookData {
        private String reference;
        private BigDecimal amount;
        private String status;
        private Customer customer;
        private LocalDateTime paidAt;
    }

    @Data
    public static class Customer {
        private String email;
    }

    public PaymentWebhookRequest toPaymentWebhookRequest() {
        return PaymentWebhookRequest.builder()
                .reference(this.data.getReference())
                .amount(this.data.getAmount().divide(BigDecimal.valueOf(100))) // Convert to Naira
                .status(this.data.getStatus())
                .customerEmail(this.data.getCustomer().getEmail())
                .paidAt(this.data.getPaidAt())
                .paymentMethod(PaymentMethod.PAYSTACK)
                .build();
    }
}