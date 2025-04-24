package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.PaymentRequest;
import examination.teacherAndStudents.entity.Payment;

public interface FeePaymentService
{
    Payment processPayment(PaymentRequest paymentDTO);
}
