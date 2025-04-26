package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.StudentTermDetailedResponse;
import examination.teacherAndStudents.dto.StudentTermRequest;
import examination.teacherAndStudents.service.StudentTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student-terms")
@RequiredArgsConstructor
public class StudentTermController {

    private final StudentTermService studentTermService;

    @PostMapping
    public ResponseEntity<ApiResponse<StudentTermDetailedResponse>> createStudentTerm(@RequestBody StudentTermRequest request) {
        StudentTermDetailedResponse response = studentTermService.createStudentTerm(request);
        ApiResponse<StudentTermDetailedResponse> apiResponse = new ApiResponse<>("Student term created successfully", true, response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentTermDetailedResponse>> updateStudentTerm(@PathVariable Long id, @RequestBody StudentTermRequest request) {
        StudentTermDetailedResponse response = studentTermService.updateStudentTerm(id, request);
        ApiResponse<StudentTermDetailedResponse> apiResponse = new ApiResponse<>("Student term updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudentTerm(@PathVariable Long id) {
        studentTermService.deleteStudentTerm(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Student term deleted successfully", true, null);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<StudentTermDetailedResponse>>> getAllStudentTerms(@PageableDefault(size = 20) Pageable pageable) {
        Page<StudentTermDetailedResponse> response = studentTermService.getAllStudentTerms(pageable);
        ApiResponse<Page<StudentTermDetailedResponse>> apiResponse = new ApiResponse<>("Student terms fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentTermDetailedResponse>> getStudentTermById(@PathVariable Long id) {
        StudentTermDetailedResponse response = studentTermService.getStudentTermById(id);
        ApiResponse<StudentTermDetailedResponse> apiResponse = new ApiResponse<>("Student term fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }
}
