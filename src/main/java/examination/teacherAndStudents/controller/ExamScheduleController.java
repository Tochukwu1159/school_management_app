package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.BulkExamScheduleRequest;
import examination.teacherAndStudents.dto.ExamScheduleRequest;
import examination.teacherAndStudents.dto.ExamScheduleResponse;
import examination.teacherAndStudents.service.ExamScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exam-schedules")
@RequiredArgsConstructor
public class ExamScheduleController {

    private final ExamScheduleService examScheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExamScheduleResponse>> createExamSchedule(@Valid @RequestBody ExamScheduleRequest request) {
        ExamScheduleResponse response = examScheduleService.createExamSchedule(request);
        ApiResponse<ExamScheduleResponse> apiResponse = new ApiResponse<>(
                "Exam schedule created successfully",
                true,
                response
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<List<ExamScheduleResponse>>> createBulkExamSchedules(@Valid @RequestBody BulkExamScheduleRequest request) {
        List<ExamScheduleResponse> responses = examScheduleService.createBulkExamSchedules(request);
        ApiResponse<List<ExamScheduleResponse>> apiResponse = new ApiResponse<>(
                "Bulk exam schedules created successfully",
                true,
                responses
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamScheduleResponse>> updateExamSchedule(
            @PathVariable Long id, @Valid @RequestBody ExamScheduleRequest request) {
        ExamScheduleResponse response = examScheduleService.updateExamSchedule(id, request);
        ApiResponse<ExamScheduleResponse> apiResponse = new ApiResponse<>(
                "Exam schedule updated successfully",
                true,
                response
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExamSchedule(@PathVariable Long id) {
        examScheduleService.deleteExamSchedule(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                "Exam schedule deleted successfully",
                true,
                null
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExamScheduleResponse>>> getAllExamSchedules(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) LocalDate examDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExamScheduleResponse> schedules = examScheduleService.getAllExamSchedules(subjectId, teacherId, examDate, pageable);
        ApiResponse<Page<ExamScheduleResponse>> apiResponse = new ApiResponse<>(
                "Exam schedules fetched successfully",
                true,
                schedules
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}