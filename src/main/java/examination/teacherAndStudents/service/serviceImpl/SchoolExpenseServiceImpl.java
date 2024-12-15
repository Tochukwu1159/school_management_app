package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.SchoolExpenseRequest;
import examination.teacherAndStudents.dto.SchoolExpenseResponse;
import examination.teacherAndStudents.entity.SchoolExpense;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.SchoolExpenseRepository;
import examination.teacherAndStudents.service.SchoolExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolExpenseServiceImpl implements SchoolExpenseService {
    private final SchoolExpenseRepository expenseRepository;

    @Override
    public SchoolExpenseResponse createExpense(SchoolExpenseRequest request) {
        try {
            SchoolExpense expense = new SchoolExpense();
            expense.setAmount(request.getAmount());
            expense.setPurpose(request.getPurpose());
            expense.setType(request.getType());
            SchoolExpense savedExpense = expenseRepository.save(expense);
            return mapToResponse(savedExpense);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error creating expense "+ e);
        }
    }

    @Override
    public SchoolExpenseResponse editExpense(Long id, SchoolExpenseRequest request) {
        try {
            SchoolExpense expense = expenseRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Expense not found"));
            expense.setAmount(request.getAmount());
            expense.setPurpose(request.getPurpose());
            expense.setType(request.getType());
            SchoolExpense updatedExpense = expenseRepository.save(expense);
            return mapToResponse(updatedExpense);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error editing expense "+ e);
        }
    }

    @Override
    public void deleteExpense(Long id) {
        try {
            expenseRepository.deleteById(id);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error deleting expense "+ e);
        }
    }

    @Override
    public Page<SchoolExpenseResponse> findAllExpenses(Pageable pageable) {
        try {
            Page<SchoolExpense> expensesPage = expenseRepository.findAll(pageable);
            return expensesPage.map(this::mapToResponse);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching all expenses "+ e);
        }
    }

    @Override
    public SchoolExpenseResponse findExpenseById(Long id) {
        try {
            SchoolExpense expense = expenseRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Expense not found"));
            return mapToResponse(expense);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error finding expense by ID "+ e);
        }
    }

    private SchoolExpenseResponse mapToResponse(SchoolExpense expense) {
        SchoolExpenseResponse response = new SchoolExpenseResponse();
        response.setId(expense.getId());
        response.setAmount(expense.getAmount());
        response.setPurpose(expense.getPurpose());
        response.setType(expense.getType());
        return response;
    }
}

