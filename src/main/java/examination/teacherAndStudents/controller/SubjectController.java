package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.SubjectRequest;
import examination.teacherAndStudents.dto.SubjectResponse;
import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public SubjectResponse createSubject(@RequestBody SubjectRequest subject) {
        return subjectService.createSubject(subject);
    }

    @PutMapping("/{subjectId}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody SubjectRequest updatedSubjectRequest
    ) {
        SubjectResponse updatedSubject = subjectService.updateSubject(subjectId, updatedSubjectRequest);
        return ResponseEntity.ok(updatedSubject);

    }

    @GetMapping("/{subjectId}")
    public SubjectResponse getSubjectById(@PathVariable Long subjectId) {
        return subjectService.findSubjectById(subjectId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Page<SubjectResponse>> getAllSubjects(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        try {
            Page<SubjectResponse> subjects = subjectService.findAllSubjects(name, page, size, sortBy, sortDirection);
            return ResponseEntity.ok(subjects);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid request parameters: " + e.getMessage());
        }
    }

    // Additional endpoints for subject-related operations
}