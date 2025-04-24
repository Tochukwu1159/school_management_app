package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class PaystackWebhookPayload {
    private String event;
    private PayStackVerificationResponse.TransactionData data;
}
