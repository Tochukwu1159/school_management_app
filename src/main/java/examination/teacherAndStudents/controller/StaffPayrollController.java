package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.StaffPayrollRequest;
import examination.teacherAndStudents.dto.StaffPayrollResponse;
import examination.teacherAndStudents.service.StaffPayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class StaffPayrollController {

    private final StaffPayrollService staffPayrollService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<StaffPayrollResponse>> createOrUpdatePayroll(@RequestBody StaffPayrollRequest payrollRequest) {
        StaffPayrollResponse createdPayroll = staffPayrollService.createOrUpdatePayroll(payrollRequest);
        ApiResponse<StaffPayrollResponse> apiResponse = new ApiResponse<>("Payroll created/updated successfully", true, createdPayroll);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/school")
    public ResponseEntity<ApiResponse<List<StaffPayrollResponse>>> getPayrollForSchool() {
        List<StaffPayrollResponse> payrolls = staffPayrollService.getPayrollForSchool();
        ApiResponse<List<StaffPayrollResponse>> apiResponse = new ApiResponse<>("Payrolls fetched successfully for school", true, payrolls);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<StaffPayrollResponse>> getPayrollForStaff(@PathVariable Long staffId) {
        StaffPayrollResponse payroll = staffPayrollService.getPayrollForStaff(staffId);
        ApiResponse<StaffPayrollResponse> apiResponse = new ApiResponse<>("Payroll fetched successfully for staff", true, payroll);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/delete/{payrollId}")
    public ResponseEntity<ApiResponse<Void>> deletePayroll(@PathVariable Long payrollId) {
        staffPayrollService.deletePayroll(payrollId);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Payroll deleted successfully", true, null);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/promote/{userId}/{staffLevelId}")
    public ResponseEntity<ApiResponse<String>> promoteStaff(@PathVariable Long userId, @PathVariable Long staffLevelId) {
        String response = staffPayrollService.promoteStaff(userId, staffLevelId);
        ApiResponse<String> apiResponse = new ApiResponse<>("Staff promoted successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<StaffPayrollResponse>>> getAllPayroll() {
        List<StaffPayrollResponse> payrolls = staffPayrollService.getAllPayroll();
        ApiResponse<List<StaffPayrollResponse>> apiResponse = new ApiResponse<>("All payrolls fetched successfully", true, payrolls);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/month/{month}/{year}")
    public ResponseEntity<ApiResponse<List<StaffPayrollResponse>>> getPayrollForMonth(@PathVariable int month, @PathVariable int year) {
        List<StaffPayrollResponse> payrolls = staffPayrollService.getPayrollForMonth(month, year);
        ApiResponse<List<StaffPayrollResponse>> apiResponse = new ApiResponse<>("Payrolls fetched successfully for month and year", true, payrolls);
        return ResponseEntity.ok(apiResponse);
    }
}
