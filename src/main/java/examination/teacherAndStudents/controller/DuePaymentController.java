package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.DuePaymentRequest;
import examination.teacherAndStudents.dto.DuePaymentResponse;
import examination.teacherAndStudents.service.DuePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<DuePaymentResponse>> getAllDuePayments() {
        List<DuePaymentResponse> duePayments = duePaymentService.getAllDuePayments();
        return new ResponseEntity<>(duePayments, HttpStatus.OK);
    }

    // Get all DuePayments by User
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DuePaymentResponse>> getAllDuePaymentsByUser(@PathVariable Long userId) {
        List<DuePaymentResponse> duePayments = duePaymentService.getAllDuePaymentsByUser(userId);
        return new ResponseEntity<>(duePayments, HttpStatus.OK);
    }

    // Delete DuePayment by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDuePayment(@PathVariable Long id) {
        duePaymentService.deleteDuePaymentById(id);
        return new ResponseEntity<>("DuePayment with id " + id + " deleted successfully", HttpStatus.OK);
    }
}
