package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.SubjectScheduleTeacherUpdateDto;
import examination.teacherAndStudents.dto.TeacherAttendanceRequest;
import examination.teacherAndStudents.dto.TeacherAttendanceResponse;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.TeacherAttendance;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.service.AdminService;
import examination.teacherAndStudents.service.TeacherAttendanceService;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class AdminController {

    private final AdminService userService;

    private final TeacherAttendanceService teacherAttendanceService;

    public AdminController(AdminService userService, TeacherAttendanceService teacherAttendanceService) {
        this.userService = userService;
        this.teacherAttendanceService = teacherAttendanceService;
    }

    @GetMapping("/students")
    public ResponseEntity<List<User>> getAllStudents() {
        try {
            List<User> studentsList = userService.getAllStudents();
            return ResponseEntity.ok(studentsList);
        } catch (CustomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // You can customize the response as needed
        } catch (CustomInternalServerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // You can customize the response as needed
        }
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<User>> getAllTeachers() {
        try {
            List<User> teachersList = userService.getAllTeachers();
            return ResponseEntity.ok(teachersList);
        } catch (CustomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // You can customize the response as needed
        } catch (CustomInternalServerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // You can customize the response as needed
        }
    }


    @PostMapping("/take-teacher-attendance")
    public ResponseEntity<Object> takeTeacherAttendance(@RequestBody TeacherAttendanceRequest attendanceRequest) {
        // Validate inputs if needed

        teacherAttendanceService.takeTeacherAttendance(attendanceRequest);

        return ResponseEntity.ok("Teacher attendance taken successfully");
    }

    @GetMapping("/calculate-teacher-percentage/{userId}/{sessionId}/{term}")
    public ResponseEntity<TeacherAttendanceResponse> calculateAttendancePercentage(@PathVariable Long userId,@PathVariable Long term, @PathVariable Long sessionId) {
        try {
            TeacherAttendanceResponse attendancePercentage = teacherAttendanceService.calculateAttendancePercentage(userId, sessionId, term);
            return ResponseEntity.ok(attendancePercentage);
        } catch (CustomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/teacher-attendance")
    public ResponseEntity<List<TeacherAttendance>> getAllTeacherAttendance() {
        List<TeacherAttendance> attendanceList = teacherAttendanceService.getAllTeacherAttendance();
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/teacher-attendance/search")
    public ResponseEntity<List<TeacherAttendance>> getTeacherAttendanceByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeacherAttendance> attendanceList = teacherAttendanceService.getTeacherAttendanceByDateRange(startDate, endDate);
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/search/teacher/date")
    public ResponseEntity<List<TeacherAttendance>> getTeacherAttendanceByTeacherAndDateRange(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeacherAttendance> attendanceList = teacherAttendanceService.getTeacherAttendanceByTeacherAndDateRange(teacherId, startDate, endDate);
        return ResponseEntity.ok(attendanceList);

    }

    @PostMapping("/calculate-teacher-attendance")
    public ResponseEntity<List<TeacherAttendanceResponse>> calculateTeacherAttendancePercentage(@RequestParam Long sessionId, @RequestParam Long termId) {
        try {
            List<TeacherAttendanceResponse> attendancePercentage = teacherAttendanceService.calculateTeacherAttendancePercentage(sessionId, termId);
            return ResponseEntity.ok(attendancePercentage);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (CustomInternalServerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/update-teaching-status")
    public ResponseEntity<SubjectSchedule> updateTeachingStatus(
            @RequestBody SubjectScheduleTeacherUpdateDto updateDto
          ) {
        SubjectSchedule updatedSchedule = teacherAttendanceService.updateTeachingStatus(updateDto);
        return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
    }
}