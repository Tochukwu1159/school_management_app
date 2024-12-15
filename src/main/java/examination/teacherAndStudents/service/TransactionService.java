package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.TransactionResponse;

import java.util.List;

public interface TransactionService {

    List<TransactionResponse> getStudentTransactions(int offset, int pageSize) throws Exception;

//    List<TransactionResponse> getTeacherTransactions(int offset, int pageSize);
}
