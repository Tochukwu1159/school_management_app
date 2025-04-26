package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<String>> payStaff(
            @RequestParam Long schoolId,
            @RequestParam int month,
            @RequestParam int year) {

        try {
            String result = payStackBulkTransfer.payStaff(schoolId, month, year);
            ApiResponse<String> response = new ApiResponse<>("Payment processed successfully", true, result);
            return ResponseEntity.ok(response); // Wrap the result in ApiResponse
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Payment processing failed: " + e.getMessage(), false, null);
            return ResponseEntity.status(500).body(response); // Wrap error message in ApiResponse
        }
    }

}
