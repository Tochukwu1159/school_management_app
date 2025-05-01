package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.PaymentRequest;
import examination.teacherAndStudents.dto.PaymentWithoutFeeIdRequest;

public interface FeePaymentService
{
    void processPayment(PaymentRequest paymentDTO);
    void processPaymentWithoutFeeId(PaymentWithoutFeeIdRequest paymentDTO);
}
