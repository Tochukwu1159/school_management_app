package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class PayStackInitResponse {
    private boolean status;
    private String message;
    private PayStackData data;

    @Data
    public static class PayStackData {
        private String authorizationUrl;
        private String accessCode;
        private String reference;
    }

    public PaymentInitResponse toPaymentInitResponse() {
        return PaymentInitResponse.builder()
                .status(this.status)
                .message(this.message)
                .authorizationUrl(this.data.getAuthorizationUrl())
                .reference(this.data.getReference())
                .build();
    }
}