package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.SchoolExpenseRequest;
import examination.teacherAndStudents.dto.SchoolExpenseResponse;
import examination.teacherAndStudents.service.SchoolExpenseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expenses")
public class SchoolExpenseController {
    private final SchoolExpenseService expenseService;

    public SchoolExpenseController(SchoolExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/create")
    public ResponseEntity<SchoolExpenseResponse> createExpense(@RequestBody SchoolExpenseRequest request) {
        SchoolExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<SchoolExpenseResponse> editExpense(@PathVariable Long id, @RequestBody SchoolExpenseRequest request) {
        SchoolExpenseResponse response = expenseService.editExpense(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<Page<SchoolExpenseResponse>> findAllExpenses(Pageable pageable) {
        Page<SchoolExpenseResponse> expenses = expenseService.findAllExpenses(pageable);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolExpenseResponse> findExpenseById(@PathVariable Long id) {
        SchoolExpenseResponse response = expenseService.findExpenseById(id);
        return ResponseEntity.ok(response);
    }
}

