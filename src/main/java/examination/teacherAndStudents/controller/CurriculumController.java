package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.CurriculumRequest;
import examination.teacherAndStudents.dto.CurriculumResponse;
import examination.teacherAndStudents.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/curriculums")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @PostMapping("/class-subjects/{classSubjectId}")
    public ResponseEntity<ApiResponse<CurriculumResponse>> addCurriculumToClassSubject(
            @PathVariable Long classSubjectId,
            @RequestBody CurriculumRequest request) {
        CurriculumResponse response = curriculumService.addCurriculumToClassSubject(classSubjectId, request);
        return ResponseEntity.status(201).body(new ApiResponse<>("Curriculum added successfully", true, response));
    }

    @PutMapping("/{curriculumId}")
    public ResponseEntity<ApiResponse<CurriculumResponse>> updateCurriculum(
            @PathVariable Long curriculumId,
            @RequestBody CurriculumRequest request) {
        CurriculumResponse response = curriculumService.updateCurriculum(curriculumId, request);
        return ResponseEntity.ok(new ApiResponse<>("Curriculum updated successfully", true, response));
    }

    @GetMapping("/{curriculumId}")
    public ResponseEntity<ApiResponse<CurriculumResponse>> getCurriculumById(@PathVariable Long curriculumId) {
        CurriculumResponse response = curriculumService.getCurriculumById(curriculumId);
        return ResponseEntity.ok(new ApiResponse<>("Curriculum retrieved successfully", true, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CurriculumResponse>>> getAllCurriculums() {
        List<CurriculumResponse> responses = curriculumService.getAllCurriculums();
        return ResponseEntity.ok(new ApiResponse<>("Curriculums retrieved successfully", true, responses));
    }

    @DeleteMapping("/{curriculumId}")
    public ResponseEntity<ApiResponse<Void>> deleteCurriculum(@PathVariable Long curriculumId) {
        curriculumService.deleteCurriculum(curriculumId);
        return ResponseEntity.ok(new ApiResponse<>("Curriculum deleted successfully", true));
    }
}