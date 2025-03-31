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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/take")
    public ResponseEntity<String> takeAttendance(@RequestBody BulkAttendanceRequest attendanceRequest) {
        try {
            // Assuming you have a method in the service to handle attendance
            attendanceService.takeBulkAttendance(attendanceRequest);
            return ResponseEntity.ok("Attendance taken successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error taking attendance: " + e.getMessage());
        }
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<Page<AttendanceResponses>> getAttendance(
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

        return ResponseEntity.ok(response);
    }

    @GetMapping("/calculate-attendance-percentage")
    public ResponseEntity<StudentAttendanceResponse> calculateAttendancePercentage(@RequestBody AttendancePercentageRequest attendancePercentageRequest) {
        try {
            StudentAttendanceResponse attendancePercentage = attendanceService.calculateAttendancePercentage(attendancePercentageRequest.getUserId(), attendancePercentageRequest.getClassLevelId(), attendancePercentageRequest.getSessionId(), attendancePercentageRequest.getStudentTermId());
            return ResponseEntity.ok(attendancePercentage);
        } catch (CustomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/students-by-class")
    public ResponseEntity<List<Attendance>> getStudentAttendanceByClass(
            @RequestParam Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        List<Attendance> studentAttendanceList = attendanceService.getStudentAttendanceByClass(classId, startDate, endDate);
        return ResponseEntity.ok(studentAttendanceList);
    }

    @GetMapping("/class/{classLevelId}/session/{sessionId}/term/{studentTermId}/percentage")
    public ResponseEntity<List<StudentAttendanceResponse>> calculateClassAttendancePercentage(
            @PathVariable Long classLevelId,
            @PathVariable Long studentTermId,
            @PathVariable Long  sessionId) {
        List<StudentAttendanceResponse> attendancePercentages = attendanceService.calculateClassAttendancePercentage(classLevelId, sessionId, studentTermId);
        return ResponseEntity.ok(attendancePercentages);
    }

    // Add more methods for fetching attendance, generating reports, etc.
}
