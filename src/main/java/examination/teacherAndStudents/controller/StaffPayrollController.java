package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.StaffPayrollRequest;
import examination.teacherAndStudents.dto.StaffPayrollResponse;
import examination.teacherAndStudents.service.StaffPayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll")
public class StaffPayrollController {

    @Autowired
    private StaffPayrollService staffPayrollService;

    // Create or update payroll entry based on StatePayrollRequest
    @PostMapping("/create")
    public ResponseEntity<StaffPayrollResponse> createOrUpdatePayroll(@RequestBody StaffPayrollRequest payrollRequest) {
        StaffPayrollResponse createdPayroll = staffPayrollService.createOrUpdatePayroll(payrollRequest);
        return new ResponseEntity<>(createdPayroll, HttpStatus.CREATED);
    }

    // Get payroll for a specific school
    @GetMapping("/school")
    public ResponseEntity<List<StaffPayrollResponse>> getPayrollForSchool() {
        List<StaffPayrollResponse> payrolls = staffPayrollService.getPayrollForSchool();
        return new ResponseEntity<>(payrolls, HttpStatus.OK);
    }

    // Get payroll for a specific staff member
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<StaffPayrollResponse> getPayrollForStaff(@PathVariable Long staffId) {
        StaffPayrollResponse payroll = staffPayrollService.getPayrollForStaff(staffId);
        return new ResponseEntity<>(payroll, HttpStatus.OK);
    }

    // Delete a payroll entry
    @DeleteMapping("/delete/{payrollId}")
    public ResponseEntity<Void> deletePayroll(@PathVariable Long payrollId) {
        staffPayrollService.deletePayroll(payrollId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/promote/{userId}/{staffLevelId}")
    public ResponseEntity<String> promoteStaff(@PathVariable Long userId, @PathVariable Long staffLevelId) {
        String response = staffPayrollService.promoteStaff(userId, staffLevelId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    // List all payroll entries
    @GetMapping("/all")
    public ResponseEntity<List<StaffPayrollResponse>> getAllPayroll() {
        List<StaffPayrollResponse> payrolls = staffPayrollService.getAllPayroll();
        return new ResponseEntity<>(payrolls, HttpStatus.OK);
    }

    // Get payroll for a specific month and year
    @GetMapping("/month/{month}/{year}")
    public ResponseEntity<List<StaffPayrollResponse>> getPayrollForMonth(@PathVariable int month, @PathVariable int year) {
        List<StaffPayrollResponse> payrolls = staffPayrollService.getPayrollForMonth(month, year);
        return new ResponseEntity<>(payrolls, HttpStatus.OK);
    }
}
