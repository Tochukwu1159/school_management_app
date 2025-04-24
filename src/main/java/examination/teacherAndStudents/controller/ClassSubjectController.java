package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ClassSubjectRequest;
import examination.teacherAndStudents.dto.ClassSubjectResponse;
import examination.teacherAndStudents.dto.TeacherAssignmentRequest;
import examination.teacherAndStudents.service.serviceImpl.ClassSubjectServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/class-subjects")
public class ClassSubjectController {

    private final ClassSubjectServiceImpl classSubjectService;

    @Autowired
    public ClassSubjectController(ClassSubjectServiceImpl classSubjectService) {
        this.classSubjectService = classSubjectService;
    }

    @PostMapping
    public ResponseEntity<ClassSubjectResponse> createOrUpdateClassSubject(@Valid @RequestBody ClassSubjectRequest classSubjectRequest) {
        ClassSubjectResponse classSubjectResponse = classSubjectService.saveClassSubject(classSubjectRequest);
        return new ResponseEntity<>(classSubjectResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassSubjectResponse> getClassSubjectById(@PathVariable Long id) {
        ClassSubjectResponse classSubjectResponse = classSubjectService.getClassSubjectById(id);
        return new ResponseEntity<>(classSubjectResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClassSubject(@PathVariable Long id) {
        classSubjectService.deleteClassSubject(id);
        return new ResponseEntity<>("ClassSubject with id " + id + " deleted successfully", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<ClassSubjectResponse>> getAllClassSubjects(
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
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @PostMapping("/assign-teacher")
    public ResponseEntity<String> assignClassSubjectToTeacher(@Valid @RequestBody TeacherAssignmentRequest request) {
        classSubjectService.assignClassSubjectToTeacher(request);
        return new ResponseEntity<>("Teachers assigned to class subjects successfully", HttpStatus.OK);
    }

    @PutMapping("/assign-teacher")
    public ResponseEntity<String> updateClassSubjectTeacherAssignment(@Valid @RequestBody TeacherAssignmentRequest request) {
        classSubjectService.updateClassSubjectTeacherAssignment(request);
        return new ResponseEntity<>("Teacher assignments updated successfully", HttpStatus.OK);
    }
}