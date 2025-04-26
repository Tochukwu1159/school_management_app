package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.service.AttendanceService;
import examination.teacherAndStudents.utils.AttendanceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/take")
    public ResponseEntity<ApiResponse<String>> takeAttendance(@RequestBody BulkAttendanceRequest attendanceRequest) {
        try {
            attendanceService.takeBulkAttendance(attendanceRequest);
            return ResponseEntity.ok(new ApiResponse<>("Attendance taken successfully", true, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error taking attendance: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<Page<AttendanceResponses>>> getAttendance(
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long studentTermId,
            @RequestParam(required = false) Long classBlockId,
            @RequestParam(required = false) Long userProfileId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<AttendanceResponses> response = attendanceService.getStudentAttendance(
                academicYearId,
                studentTermId,
                classBlockId,
                userProfileId,
                status,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                sortDirection);

        return ResponseEntity.ok(new ApiResponse<>("Attendance retrieved successfully", true, response));
    }

    @GetMapping("/calculate-attendance-percentage")
    public ResponseEntity<ApiResponse<StudentAttendanceResponse>> calculateAttendancePercentage(@RequestBody AttendancePercentageRequest attendancePercentageRequest) {
        try {
            StudentAttendanceResponse attendancePercentage = attendanceService.calculateAttendancePercentage(
                    attendancePercentageRequest.getUserId(),
                    attendancePercentageRequest.getClassLevelId(),
                    attendancePercentageRequest.getSessionId(),
                    attendancePercentageRequest.getStudentTermId());
            return ResponseEntity.ok(new ApiResponse<>("Attendance percentage calculated successfully", true, attendancePercentage));
        } catch (CustomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Data not found", false, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error calculating attendance percentage: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/students-by-class")
    public ResponseEntity<ApiResponse<List<Attendance>>> getStudentAttendanceByClass(
            @RequestParam Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {

        List<Attendance> studentAttendanceList = attendanceService.getStudentAttendanceByClass(classId, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>("Student attendance by class retrieved successfully", true, studentAttendanceList));
    }

    @GetMapping("/class/{classLevelId}/session/{sessionId}/term/{studentTermId}/percentage")
    public ResponseEntity<ApiResponse<List<StudentAttendanceResponse>>> calculateClassAttendancePercentage(
            @PathVariable Long classLevelId,
            @PathVariable Long studentTermId,
            @PathVariable Long sessionId) {

        List<StudentAttendanceResponse> attendancePercentages = attendanceService.calculateClassAttendancePercentage(classLevelId, sessionId, studentTermId);
        return ResponseEntity.ok(new ApiResponse<>("Class attendance percentage calculated successfully", true, attendancePercentages));
    }

    // Add more methods for fetching attendance, generating reports, etc.
}
