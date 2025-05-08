package examination.teacherAndStudents.controller;

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
    public ResponseEntity<ExamScheduleResponse> createExamSchedule(@Valid @RequestBody ExamScheduleRequest request) {
        ExamScheduleResponse response = examScheduleService.createExamSchedule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/create")
    public ResponseEntity<List<ExamScheduleResponse>> createBulkExamSchedules(@Valid @RequestBody BulkExamScheduleRequest request) {
        List<ExamScheduleResponse> responses = examScheduleService.createBulkExamSchedules(request);
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamScheduleResponse> updateExamSchedule(
            @PathVariable Long id, @Valid @RequestBody ExamScheduleRequest request) {
        ExamScheduleResponse response = examScheduleService.updateExamSchedule(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExamSchedule(@PathVariable Long id) {
        examScheduleService.deleteExamSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ExamScheduleResponse>> getAllExamSchedules(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) LocalDate examDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExamScheduleResponse> schedules = examScheduleService.getAllExamSchedules(subjectId, teacherId, examDate, pageable);
        return ResponseEntity.ok(schedules);
    }
}