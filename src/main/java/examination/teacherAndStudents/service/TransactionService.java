package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.TransactionResponse;
import examination.teacherAndStudents.entity.Wallet;
import examination.teacherAndStudents.utils.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    List<TransactionResponse> getProfileTransactions(int offset, int pageSize) throws Exception;
    void recordTransaction(Wallet wallet, BigDecimal amount, TransactionType transactionType, String description);
}
