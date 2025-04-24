package examination.teacherAndStudents.service;

import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    void recordFailedPayment(String reference, String failureReason,
                             String provider, String customerEmail);
    boolean paymentExists(String reference);
}