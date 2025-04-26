package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
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

    /**
     * Create a new expense
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SchoolExpenseResponse>> createExpense(@RequestBody SchoolExpenseRequest request) {
        try {
            SchoolExpenseResponse response = expenseService.createExpense(request);
            ApiResponse<SchoolExpenseResponse> apiResponse = new ApiResponse<>("Expense created successfully", true, response);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (Exception e) {
            // Returning null data with error message
            ApiResponse<SchoolExpenseResponse> errorResponse = new ApiResponse<>("Error creating expense: " + e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Edit an existing expense
     */
    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<SchoolExpenseResponse>> editExpense(@PathVariable Long id, @RequestBody SchoolExpenseRequest request) {
        try {
            SchoolExpenseResponse response = expenseService.editExpense(id, request);
            ApiResponse<SchoolExpenseResponse> apiResponse = new ApiResponse<>("Expense edited successfully", true, response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            // Returning null data with error message
            ApiResponse<SchoolExpenseResponse> errorResponse = new ApiResponse<>("Error editing expense: " + e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete an expense by ID
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            // Returning void in case of successful deletion
            ApiResponse<Void> response = new ApiResponse<>("Expense deleted successfully", true, null);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        } catch (Exception e) {
            // Returning error message for deletion failure
            ApiResponse<Void> errorResponse = new ApiResponse<>("Error deleting expense: " + e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Find all expenses with pagination
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<SchoolExpenseResponse>>> findAllExpenses(Pageable pageable) {
        try {
            Page<SchoolExpenseResponse> expenses = expenseService.findAllExpenses(pageable);
            ApiResponse<Page<SchoolExpenseResponse>> response = new ApiResponse<>("Expenses fetched successfully", true, expenses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Returning null data with error message
            ApiResponse<Page<SchoolExpenseResponse>> errorResponse = new ApiResponse<>("Error fetching expenses: " + e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Find a specific expense by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolExpenseResponse>> findExpenseById(@PathVariable Long id) {
        try {
            SchoolExpenseResponse response = expenseService.findExpenseById(id);
            ApiResponse<SchoolExpenseResponse> apiResponse = new ApiResponse<>("Expense fetched successfully", true, response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            // Returning null data with error message
            ApiResponse<SchoolExpenseResponse> errorResponse = new ApiResponse<>("Error fetching expense: " + e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
