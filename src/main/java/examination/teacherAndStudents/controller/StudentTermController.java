package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.StudentTermDetailedResponse;
import examination.teacherAndStudents.dto.StudentTermRequest;
import examination.teacherAndStudents.dto.StudentTermResponse;import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.service.StudentTermService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<StudentTermDetailedResponse> createStudentTerm(@RequestBody StudentTermRequest request) {
        StudentTermDetailedResponse response = studentTermService.createStudentTerm(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentTermDetailedResponse> updateStudentTerm(@PathVariable Long id, @RequestBody StudentTermRequest request) {
        StudentTermDetailedResponse response = studentTermService.updateStudentTerm(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentTerm(@PathVariable Long id) {
        studentTermService.deleteStudentTerm(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<StudentTermDetailedResponse>> getAllStudentTerms( @PageableDefault(size = 20) Pageable pageable) {
        Page<StudentTermDetailedResponse> response = studentTermService.getAllStudentTerms(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentTermDetailedResponse> getStudentTermById(@PathVariable Long id) {
        StudentTermDetailedResponse response = studentTermService.getStudentTermById(id);
        return ResponseEntity.ok(response);
    }
}