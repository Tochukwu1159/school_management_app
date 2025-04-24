package examination.teacherAndStudents.service.funding;

import com.fasterxml.jackson.databind.ObjectMapper;
import examination.teacherAndStudents.dto.PaymentInitResponse;
import examination.teacherAndStudents.dto.PaymentRequestDto;
import examination.teacherAndStudents.dto.PaymentVerificationResponse;
import examination.teacherAndStudents.dto.WebhookRequest;


public interface PaymentProvider {
    PaymentInitResponse initiatePayment(PaymentRequestDto request);
    PaymentVerificationResponse verifyPayment(String reference);
    boolean supportsWebhook(String provider);
    void handleWebhook(WebhookRequest request);
}



