package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ApplicationReviewDto;
import examination.teacherAndStudents.dto.PaymentProviderRequest;
import examination.teacherAndStudents.dto.PaymentResponse;
import examination.teacherAndStudents.dto.UserResponse;

public interface ApplicationService {
    PaymentResponse payApplicationFee(Long applicationId, PaymentProviderRequest paymentRequest);
    UserResponse reviewApplication(Long applicationId, ApplicationReviewDto review);
}
