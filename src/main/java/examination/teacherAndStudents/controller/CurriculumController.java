package examination.teacherAndStudents.controller;

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

    // Add a new curriculum to a class subject
    @PostMapping("/class-subjects/{classSubjectId}")
    public ResponseEntity<CurriculumResponse> addCurriculumToClassSubject(
            @PathVariable Long classSubjectId,
            @RequestBody CurriculumRequest request) {
        CurriculumResponse response = curriculumService.addCurriculumToClassSubject(classSubjectId, request);
        return ResponseEntity.ok(response);
    }

    // Update an existing curriculum
    @PutMapping("/{curriculumId}")
    public ResponseEntity<CurriculumResponse> updateCurriculum(
            @PathVariable Long curriculumId,
            @RequestBody CurriculumRequest request) {
        CurriculumResponse response = curriculumService.updateCurriculum(curriculumId, request);
        return ResponseEntity.ok(response);
    }

    // Get a curriculum by ID
    @GetMapping("/{curriculumId}")
    public ResponseEntity<CurriculumResponse> getCurriculumById(@PathVariable Long curriculumId) {
        CurriculumResponse response = curriculumService.getCurriculumById(curriculumId);
        return ResponseEntity.ok(response);
    }

    // Get all curriculums
    @GetMapping
    public ResponseEntity<List<CurriculumResponse>> getAllCurriculums() {
        List<CurriculumResponse> responses = curriculumService.getAllCurriculums();
        return ResponseEntity.ok(responses);
    }

    // Delete a curriculum by ID
    @DeleteMapping("/{curriculumId}")
    public ResponseEntity<Void> deleteCurriculum(@PathVariable Long curriculumId) {
        curriculumService.deleteCurriculum(curriculumId);
        return ResponseEntity.noContent().build();
    }
}

