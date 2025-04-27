package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.*;

public interface ApplicationService {
    PaymentResponse payApplicationFee(Long applicationId, PaymentProviderRequest paymentRequest);
    ApplicationResponse reviewApplication(Long applicationId, ApplicationReviewDto review);
}
