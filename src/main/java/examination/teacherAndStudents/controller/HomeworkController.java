package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.HomeworkResponse;
import examination.teacherAndStudents.dto.HomeworkSubmissionResponse;
import examination.teacherAndStudents.entity.Homework;
import examination.teacherAndStudents.entity.HomeworkSubmission;
import examination.teacherAndStudents.service.serviceImpl.HomeworkService;
import examination.teacherAndStudents.utils.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;

    @PostMapping("/create")
    public ResponseEntity<Homework> createHomework(
            @RequestParam Long teacherId,
            @RequestParam Long subjectId,
            @RequestParam Long sessionId,
            @RequestParam Long classId,
            @RequestParam Long termId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String fileUrl,
            @RequestParam LocalDateTime submissionDate) {
        return ResponseEntity.ok(homeworkService.createHomework(teacherId, subjectId, sessionId, classId, termId, title, description, fileUrl, submissionDate));
    }

    @PostMapping("/submit")
    public ResponseEntity<HomeworkSubmission> submitHomework(
            @RequestParam Long homeworkId,
            @RequestParam String fileUrl) {
        return ResponseEntity.ok(homeworkService.submitHomework(homeworkId, fileUrl));
    }

    @PostMapping("/grade")
    public ResponseEntity<HomeworkSubmission> gradeSubmission(
            @RequestParam Long submissionId,
            @RequestParam Double obtainedMark) {
        return ResponseEntity.ok(homeworkService.gradeSubmission(submissionId, obtainedMark));
    }

    // For Homework Submissions
    @GetMapping("/submissions")
    public ResponseEntity<Page<HomeworkSubmissionResponse>> getSubmissionsByHomework(
            @RequestParam(required = false) Long homeworkId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime submittedAt,
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<HomeworkSubmissionResponse> response = homeworkService.getSubmissionsByHomework(
                homeworkId,
                studentId,
                submittedAt,
                status,
                page,
                size,
                sortBy,
                sortDirection);

        return ResponseEntity.ok(response);
    }

    // For Homework Assignments
    @GetMapping("/homework")
    public ResponseEntity<Page<HomeworkResponse>> getHomeworkBySubject(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long classBlockId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime submissionDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submissionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<HomeworkResponse> response = homeworkService.getHomeworkBySubject(
                subjectId,
                classBlockId,
                termId,
                submissionDate,
                page,
                size,
                sortBy,
                sortDirection);

        return ResponseEntity.ok(response);
    }
}
