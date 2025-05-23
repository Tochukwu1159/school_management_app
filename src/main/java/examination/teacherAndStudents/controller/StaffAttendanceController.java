package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.StaffAttendanceRequest;
import examination.teacherAndStudents.dto.StaffAttendanceResponse;
import examination.teacherAndStudents.entity.StaffAttendance;
import examination.teacherAndStudents.service.StaffAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/staff-attendance")
@RequiredArgsConstructor
public class StaffAttendanceController {

    private final StaffAttendanceService staffAttendanceService;


    @PostMapping("/take")
    public ResponseEntity<ApiResponse<Void>> takeStaffAttendance(@RequestBody StaffAttendanceRequest request) {
        staffAttendanceService.takeStaffAttendance(request);
        ApiResponse<Void> response = new ApiResponse<>("Staff attendance recorded successfully", true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/calculate/{staffId}")
    public ResponseEntity<ApiResponse<StaffAttendanceResponse>> calculateAttendancePercentage(
            @PathVariable Long staffId,
            @RequestParam Long sessionId,
            @RequestParam Long termId) {
        StaffAttendanceResponse response = staffAttendanceService.calculateAttendancePercentage(staffId, sessionId, termId);
        ApiResponse<StaffAttendanceResponse> apiResponse = new ApiResponse<>("Attendance percentage calculated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/calculate/all")
    public ResponseEntity<ApiResponse<List<StaffAttendanceResponse>>> calculateStaffAttendancePercentage(
            @RequestParam Long sessionId,
            @RequestParam Long termId,
            @RequestParam String role) {
        List<StaffAttendanceResponse> responses = staffAttendanceService.calculateStaffAttendancePercentage(sessionId, termId, role);
        ApiResponse<List<StaffAttendanceResponse>> apiResponse = new ApiResponse<>("Staff attendance percentages calculated successfully", true, responses);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<StaffAttendance>>> getAllStaffAttendance() {
        List<StaffAttendance> attendances = staffAttendanceService.getAllStaffAttendance();
        ApiResponse<List<StaffAttendance>> response = new ApiResponse<>("Staff attendances fetched successfully", true, attendances);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<StaffAttendance>>> getStaffAttendanceByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<StaffAttendance> attendances = staffAttendanceService.getStaffAttendanceByDateRange(startDate, endDate);
        ApiResponse<List<StaffAttendance>> response = new ApiResponse<>("Staff attendances fetched successfully", true, attendances);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff/{staffId}/date-range")
    public ResponseEntity<ApiResponse<List<StaffAttendance>>> getStaffAttendanceByStaffAndDateRange(
            @PathVariable Long staffId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<StaffAttendance> attendances = staffAttendanceService.getStaffAttendanceByStaffAndDateRange(staffId, startDate, endDate);
        ApiResponse<List<StaffAttendance>> response = new ApiResponse<>("Staff attendances fetched successfully", true, attendances);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all/paged")
    public ResponseEntity<ApiResponse<Page<StaffAttendance>>> getAllStaffAttendancePaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy) {
        Page<StaffAttendance> attendances = staffAttendanceService.getAllStaffAttendance(page, size, sortBy);
        ApiResponse<Page<StaffAttendance>> response = new ApiResponse<>("Staff attendances fetched successfully", true, attendances);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range/paged")
    public ResponseEntity<ApiResponse<Page<StaffAttendance>>> getStaffAttendanceByDateRangePaged(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy) {
        Page<StaffAttendance> attendances = staffAttendanceService.getStaffAttendanceByDateRange(startDate, endDate, page, size, sortBy);
        ApiResponse<Page<StaffAttendance>> response = new ApiResponse<>("Staff attendances fetched successfully", true, attendances);
        return ResponseEntity.ok(response);
    }
}