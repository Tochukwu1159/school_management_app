package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.HomeworkRequest;
import examination.teacherAndStudents.dto.HomeworkResponse;
import examination.teacherAndStudents.dto.HomeworkSubmissionRequest;
import examination.teacherAndStudents.dto.HomeworkSubmissionResponse;
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
    public ResponseEntity<HomeworkResponse> createHomework(@Valid @RequestBody HomeworkRequest request) {
        return ResponseEntity.ok(homeworkService.createHomework(request));
    }

    @PostMapping("/submissions")
    public ResponseEntity<HomeworkSubmissionResponse> submitHomework(
            @Valid @RequestBody HomeworkSubmissionRequest request
    ) {
        return ResponseEntity.ok(homeworkService.submitHomework(request));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<HomeworkSubmissionResponse> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestParam Double obtainedMark
    ) {
        return ResponseEntity.ok(homeworkService.gradeSubmission(submissionId, obtainedMark));
    }

    @GetMapping("/submissions")
    public ResponseEntity<Page<HomeworkSubmissionResponse>> getSubmissionsByHomework(
            @RequestParam(required = false) Long homeworkId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime submittedAt,
            @RequestParam(required = false) SubmissionStatus status,
//            @RequestParam(required = false) String studentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        return ResponseEntity.ok(homeworkService.getSubmissionsByHomework(
                homeworkId, studentId, submittedAt, status, page, size, sortBy, sortDirection
        ));
    }

    @GetMapping
    public ResponseEntity<Page<HomeworkResponse>> getHomeworkBySubject(
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
        return ResponseEntity.ok(homeworkService.getHomeworkBySubject(
                subjectId, classBlockId, termId, submissionDate, title, page, size, sortBy, sortDirection
        ));
    }
}