package examination.teacherAndStudents.service;

import examination.teacherAndStudents.service.serviceImpl.PaymentServiceImpl;
import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.StudentTerm;

public interface PaymentService {
    PaymentServiceImpl.PaymentResult payDue(Long dueId, Long termId, Long sessionId);
}
