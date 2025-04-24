package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.CheckoutRequest;
import examination.teacherAndStudents.dto.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse checkout(Long profileId);
}