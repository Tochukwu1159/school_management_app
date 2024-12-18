package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.StudentTermRequest;
import examination.teacherAndStudents.dto.StudentTermResponse;import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.service.StudentTermService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-terms")
public class StudentTermController {

    private final StudentTermService studentTermService;

    public StudentTermController(StudentTermService studentTermService) {
        this.studentTermService = studentTermService;
    }

    @PostMapping
    public ResponseEntity<StudentTermResponse> createStudentTerm(@RequestBody StudentTermRequest request) {
        StudentTermResponse response = studentTermService.createStudentTerm(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentTermResponse> updateStudentTerm(@PathVariable Long id, @RequestBody StudentTermRequest request) {
        StudentTermResponse response = studentTermService.updateStudentTerm(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentTerm(@PathVariable Long id) {
        studentTermService.deleteStudentTerm(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<StudentTermResponse>> getAllStudentTerms() {
        List<StudentTermResponse> responses = studentTermService.getAllStudentTerms();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentTermResponse> getStudentTermById(@PathVariable Long id) {
        StudentTermResponse response = studentTermService.getStudentTermById(id);
        return ResponseEntity.ok(response);
    }
}