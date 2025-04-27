package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.AccountFundingRequest;
import examination.teacherAndStudents.dto.AccountFundingResponse;
import examination.teacherAndStudents.entity.PaymentAccount;

public interface AccountFundingService {
    AccountFundingResponse initializePayment(AccountFundingRequest request) throws Exception;
}
