package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.service.serviceImpl.HomeworkService;
import examination.teacherAndStudents.utils.SubmissionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;

    @PostMapping
    public ResponseEntity<ApiResponse<HomeworkResponse>> createHomework(@Valid @RequestBody HomeworkRequest request) {
        HomeworkResponse response = homeworkService.createHomework(request);
        ApiResponse<HomeworkResponse> apiResponse = new ApiResponse<>("Homework created successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/submissions")
    public ResponseEntity<ApiResponse<HomeworkSubmissionResponse>> submitHomework(@Valid @RequestBody HomeworkSubmissionRequest request) {
        HomeworkSubmissionResponse response = homeworkService.submitHomework(request);
        ApiResponse<HomeworkSubmissionResponse> apiResponse = new ApiResponse<>("Homework submitted successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<ApiResponse<HomeworkSubmissionResponse>> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestParam Double obtainedMark
    ) {
        HomeworkSubmissionResponse response = homeworkService.gradeSubmission(submissionId, obtainedMark);
        ApiResponse<HomeworkSubmissionResponse> apiResponse = new ApiResponse<>("Submission graded successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<Page<HomeworkSubmissionResponse>>> getSubmissionsByHomework(
            @RequestParam(required = false) Long homeworkId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime submittedAt,
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Page<HomeworkSubmissionResponse> response = homeworkService.getSubmissionsByHomework(
                homeworkId, studentId, submittedAt, status, page, size, sortBy, sortDirection
        );
        ApiResponse<Page<HomeworkSubmissionResponse>> apiResponse = new ApiResponse<>("Submissions fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<HomeworkResponse>>> getHomeworkBySubject(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long classBlockId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime submissionDate,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submissionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Page<HomeworkResponse> response = homeworkService.getHomeworkBySubject(
                subjectId, classBlockId, termId, submissionDate, title, page, size, sortBy, sortDirection
        );
        ApiResponse<Page<HomeworkResponse>> apiResponse = new ApiResponse<>("Homeworks fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }
}
