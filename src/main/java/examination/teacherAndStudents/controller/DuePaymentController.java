package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.DuePaymentRequest;
import examination.teacherAndStudents.dto.DuePaymentResponse;
import examination.teacherAndStudents.service.DuePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/due-payments")
public class DuePaymentController {

    private final DuePaymentService duePaymentService;

    @Autowired
    public DuePaymentController(DuePaymentService duePaymentService) {
        this.duePaymentService = duePaymentService;
    }

    // Create or Update DuePayment
    @PostMapping
    public ResponseEntity<DuePaymentResponse> createOrUpdateDuePayment(@RequestBody DuePaymentRequest duePaymentRequest) {
        DuePaymentResponse duePaymentResponse = duePaymentService.makeDuePayment(duePaymentRequest);
        return new ResponseEntity<>(duePaymentResponse, HttpStatus.CREATED);
    }

    // Get DuePayment by ID
    @GetMapping("/{id}")
    public ResponseEntity<DuePaymentResponse> getDuePaymentById(@PathVariable Long id) {
        DuePaymentResponse duePaymentResponse = duePaymentService.getDuePaymentById(id);
        return new ResponseEntity<>(duePaymentResponse, HttpStatus.OK);
    }

    // Get all DuePayments
    @GetMapping
    public ResponseEntity<Page<DuePaymentResponse>> getAllDuePayments(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long studentTermId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long profileId,
            @RequestParam(required = false) Long dueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<DuePaymentResponse> paymentsPage = duePaymentService.getAllDuePayments(
                id,
                studentTermId,
                academicYearId,
                profileId,
                dueId,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                sortDirection);

        return new ResponseEntity<>(paymentsPage, HttpStatus.OK);
    }

//    Get all payments for user: /due-payments/user/123
//
//    Filter by due: /due-payments/user/123?dueId=456
//
//    Filter by term: /due-payments/user/123?studentTermId=1
//
//    Filter by creation date: /due-payments/user/123?createdAt=2023-01-01T00:00:00


    // Get all DuePayments by User
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<DuePaymentResponse>> getAllDuePaymentsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Long dueId,
            @RequestParam(required = false) Long studentTermId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<DuePaymentResponse> paymentsPage = duePaymentService.getAllDuePaymentsByUser(
                userId,
                dueId,
                studentTermId,
                academicYearId,
                createdAt,
                page,
                size,
                sortBy,
                sortDirection);

        return new ResponseEntity<>(paymentsPage, HttpStatus.OK);
    }
    // Delete DuePayment by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDuePayment(@PathVariable Long id) {
        duePaymentService.deleteDuePaymentById(id);
        return new ResponseEntity<>("DuePayment with id " + id + " deleted successfully", HttpStatus.OK);
    }
}
