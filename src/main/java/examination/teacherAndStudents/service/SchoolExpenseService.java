package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SchoolExpenseRequest;
import examination.teacherAndStudents.dto.SchoolExpenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SchoolExpenseService {
    SchoolExpenseResponse createExpense(SchoolExpenseRequest request);
    SchoolExpenseResponse editExpense(Long id, SchoolExpenseRequest request);
    void deleteExpense(Long id);
    Page<SchoolExpenseResponse> findAllExpenses(Pageable pageable);
    SchoolExpenseResponse findExpenseById(Long id);
}