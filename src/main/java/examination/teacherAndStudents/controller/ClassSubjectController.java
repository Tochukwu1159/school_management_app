package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.ClassSubjectRequest;
import examination.teacherAndStudents.dto.ClassSubjectResponse;
import examination.teacherAndStudents.dto.TeacherAssignmentRequest;
import examination.teacherAndStudents.service.serviceImpl.ClassSubjectServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/class-subjects")
@RequiredArgsConstructor
public class ClassSubjectController {

    private final ClassSubjectServiceImpl classSubjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClassSubjectResponse>> createOrUpdateClassSubject(@Valid @RequestBody ClassSubjectRequest classSubjectRequest) {
        ClassSubjectResponse classSubjectResponse = classSubjectService.saveClassSubject(classSubjectRequest);
        return ResponseEntity.status(201).body(new ApiResponse<>("Class subject created or updated successfully", true, classSubjectResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassSubjectResponse>> getClassSubjectById(@PathVariable Long id) {
        ClassSubjectResponse classSubjectResponse = classSubjectService.getClassSubjectById(id);
        return ResponseEntity.ok(new ApiResponse<>("Class subject retrieved successfully", true, classSubjectResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClassSubject(@PathVariable Long id) {
        classSubjectService.deleteClassSubject(id);
        return ResponseEntity.ok(new ApiResponse<>("Class subject deleted successfully", true));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClassSubjectResponse>>> getAllClassSubjects(
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long classSubjectId,
            @RequestParam(required = false) String subjectName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Page<ClassSubjectResponse> responses = classSubjectService.getAllClassSubjects(
                academicYearId, subjectId, classSubjectId, subjectName, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(new ApiResponse<>("Class subjects retrieved successfully", true, responses));
    }

    @PostMapping("/assign-teacher")
    public ResponseEntity<ApiResponse<Void>> assignClassSubjectToTeacher(@Valid @RequestBody TeacherAssignmentRequest request) {
        classSubjectService.assignClassSubjectToTeacher(request);
        return ResponseEntity.ok(new ApiResponse<>("Teachers assigned to class subjects successfully", true));
    }

    @PutMapping("/assign-teacher")
    public ResponseEntity<ApiResponse<Void>> updateClassSubjectTeacherAssignment(@Valid @RequestBody TeacherAssignmentRequest request) {
        classSubjectService.updateClassSubjectTeacherAssignment(request);
        return ResponseEntity.ok(new ApiResponse<>("Teacher assignments updated successfully", true));
    }
}