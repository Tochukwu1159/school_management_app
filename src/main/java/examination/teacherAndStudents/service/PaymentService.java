package examination.teacherAndStudents.service;

import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.StudentTerm;

public interface PaymentService {
    void payDue(Long dueId,  Long termId, Long sessionId);
}
