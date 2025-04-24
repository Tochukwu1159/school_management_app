package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class FlutterwaveWebhookPayload {
    private String event;
    private PayStackVerificationResponse.TransactionData data;
}