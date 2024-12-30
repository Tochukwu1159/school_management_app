package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.paystack.PayStackBulkTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {

    private final PayStackBulkTransfer payStackBulkTransfer;

    @PostMapping("/pay-staff")
    public ResponseEntity<String> payStaff(
            @RequestParam Long schoolId,
            @RequestParam int month,
            @RequestParam int year) {

        try {
            String result = payStackBulkTransfer.payStaff(schoolId, month, year);
            return ResponseEntity.ok(result); // Returns a success message if payment is processed
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Payment processing failed: " + e.getMessage());
        }
    }
}
