package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.SubjectRequest;
import examination.teacherAndStudents.dto.SubjectResponse;
import examination.teacherAndStudents.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(@RequestBody SubjectRequest subjectRequest) {
        SubjectResponse createdSubject = subjectService.createSubject(subjectRequest);
        ApiResponse<SubjectResponse> apiResponse = new ApiResponse<>("Subject created successfully", true, createdSubject);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody SubjectRequest updatedSubjectRequest
    ) {
        SubjectResponse updatedSubject = subjectService.updateSubject(subjectId, updatedSubjectRequest);
        ApiResponse<SubjectResponse> apiResponse = new ApiResponse<>("Subject updated successfully", true, updatedSubject);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(@PathVariable Long subjectId) {
        SubjectResponse subjectResponse = subjectService.findSubjectById(subjectId);
        ApiResponse<SubjectResponse> apiResponse = new ApiResponse<>("Subject fetched successfully", true, subjectResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<SubjectResponse>>> getAllSubjects(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        Page<SubjectResponse> subjects = subjectService.findAllSubjects(name, page, size, sortBy, sortDirection);
        ApiResponse<Page<SubjectResponse>> apiResponse = new ApiResponse<>("Subjects fetched successfully", true, subjects);
        return ResponseEntity.ok(apiResponse);
    }

}
