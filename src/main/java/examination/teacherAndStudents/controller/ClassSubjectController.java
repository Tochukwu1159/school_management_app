package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ClassSubjectRequest;
import examination.teacherAndStudents.dto.ClassSubjectResponse;
import examination.teacherAndStudents.service.serviceImpl.ClassSubjectServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/class-subjects")
public class ClassSubjectController {

    private final ClassSubjectServiceImpl classSubjectService;

    @Autowired
    public ClassSubjectController(ClassSubjectServiceImpl classSubjectService) {
        this.classSubjectService = classSubjectService;
    }

    // Create or Update ClassSubject
    @PostMapping
    public ResponseEntity<ClassSubjectResponse> createOrUpdateClassSubject(@RequestBody ClassSubjectRequest classSubjectRequest) {
        ClassSubjectResponse classSubjectResponse = classSubjectService.saveClassSubject(classSubjectRequest);
        return new ResponseEntity<>(classSubjectResponse, HttpStatus.CREATED);
    }

    // Get ClassSubject by ID
    @GetMapping("/{id}")
    public ResponseEntity<ClassSubjectResponse> getClassSubjectById(@PathVariable Long id) {
        ClassSubjectResponse classSubjectResponse = classSubjectService.getClassSubjectById(id);
        return new ResponseEntity<>(classSubjectResponse, HttpStatus.OK);
    }

    // Delete ClassSubject by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClassSubject(@PathVariable Long id) {
        classSubjectService.deleteClassSubject(id);
        return new ResponseEntity<>("ClassSubject with id " + id + " deleted successfully", HttpStatus.OK);
    }

    // Get all ClassSubjects
    @GetMapping
    public ResponseEntity<List<ClassSubjectResponse>> getAllClassSubjects() {
        List<ClassSubjectResponse> classSubjects = classSubjectService.getAllClassSubjects();
        return new ResponseEntity<>(classSubjects, HttpStatus.OK);
    }
}
