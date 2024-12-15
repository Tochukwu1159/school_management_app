package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.DuePaymentRequest;
import examination.teacherAndStudents.dto.DuePaymentResponse;

import java.util.List;

public interface DuePaymentService {
    DuePaymentResponse makeDuePayment(DuePaymentRequest duePaymentRequest);
    List<DuePaymentResponse> getAllDuePayments();
    DuePaymentResponse getDuePaymentById(Long id);
     List<DuePaymentResponse> getAllDuePaymentsByUser(Long userId);
    void deleteDuePaymentById(Long id);

}
