package examination.teacherAndStudents.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlutterwavePaymentRequest {
    private String tx_ref;
    private BigDecimal amount;
    private String currency;
    private String redirect_url;
    private String payment_options;
    private Customer customer;
    private Map<String, String> customizations;
    private Map<String, Object> meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customer {
        private String email;
        private String phonenumber;
        private String name;
    }

    // Helper method to create from standard payment request
    public static FlutterwavePaymentRequest fromStandardRequest(PaymentRequestDto request, String txRef) {
        return FlutterwavePaymentRequest.builder()
                .tx_ref(txRef)
                .amount(request.getAmount())
                .currency("NGN") // Default to Naira
                .redirect_url(request.getCallbackUrl())
                .customer(Customer.builder()
                        .email(request.getEmail())
                        .build())
                .meta(request.getMetadata())
                .build();
    }
}