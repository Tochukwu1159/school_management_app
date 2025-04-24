package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SchoolExpenseRequest;
import examination.teacherAndStudents.dto.SchoolExpenseResponse;
import examination.teacherAndStudents.entity.SchoolExpense;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.SchoolExpenseRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.SchoolExpenseService;
import examination.teacherAndStudents.utils.Roles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class SchoolExpenseServiceImpl implements SchoolExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(SchoolExpenseServiceImpl.class);

    private final SchoolExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public SchoolExpenseResponse createExpense(@Valid SchoolExpenseRequest request) {
        verifyUserAccess();
        logger.info("Creating new expense with purpose: {}", request.getPurpose());

        SchoolExpense expense = modelMapper.map(request, SchoolExpense.class);
        SchoolExpense savedExpense = expenseRepository.save(expense);
        return modelMapper.map(savedExpense, SchoolExpenseResponse.class);
    }

    @Transactional
    @Override
    public SchoolExpenseResponse editExpense(Long id, @Valid SchoolExpenseRequest request) {
        verifyUserAccess();
        logger.info("Editing expense with ID: {}", id);

        SchoolExpense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + id));
        modelMapper.map(request, expense);
        SchoolExpense updatedExpense = expenseRepository.save(expense);
        return modelMapper.map(updatedExpense, SchoolExpenseResponse.class);
    }

    @Transactional
    @Override
    public void deleteExpense(Long id) {
        verifyUserAccess();
        logger.info("Deleting expense with ID: {}", id);

        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("Expense not found with ID: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SchoolExpenseResponse> findAllExpenses(Pageable pageable) {
        verifyUserAccess();
        logger.info("Fetching all expenses with pageable: {}", pageable);

        return expenseRepository.findAll(pageable)
                .map(expense -> modelMapper.map(expense, SchoolExpenseResponse.class));
    }

    @Transactional(readOnly = true)
    @Override
    public SchoolExpenseResponse findExpenseById(Long id) {
        verifyUserAccess();
        logger.info("Fetching expense with ID: {}", id);

        SchoolExpense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + id));
        return modelMapper.map(expense, SchoolExpenseResponse.class);
    }

    private void verifyUserAccess() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        if (!user.getRoles().contains(Roles.ADMIN) || !user.getRoles().contains(Roles.DRIVER)) {
            logger.warn("Unauthorized access attempt by user: {}", email);
            throw new AuthenticationFailedException("Access restricted to ADMIN or DRIVER roles");
        }
    }
}