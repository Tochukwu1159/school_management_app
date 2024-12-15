package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.PayStackTransactionRequest;
import examination.teacherAndStudents.dto.PayStackTransactionResponse;


public interface PayStackPaymentService {
     PayStackTransactionResponse initTransaction(PayStackTransactionRequest request) throws Exception;
}

