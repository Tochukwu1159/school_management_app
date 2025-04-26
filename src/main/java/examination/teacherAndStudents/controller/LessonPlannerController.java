package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.LessonPlannerRequest;
import examination.teacherAndStudents.dto.LessonPlannerResponse;
import examination.teacherAndStudents.service.LessonPlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson-planners")
@RequiredArgsConstructor
public class LessonPlannerController {

    private final LessonPlannerService lessonPlannerService;

    @PostMapping
    public ResponseEntity<ApiResponse<LessonPlannerResponse>> createLessonPlanner(@RequestBody LessonPlannerRequest request) {
        LessonPlannerResponse response = lessonPlannerService.createLessonPlanner(request);
        ApiResponse<LessonPlannerResponse> apiResponse = new ApiResponse<>("Lesson planner created successfully", true, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonPlannerResponse>> updateLessonPlanner(@PathVariable Long id, @RequestBody LessonPlannerRequest request) {
        LessonPlannerResponse response = lessonPlannerService.updateLessonPlanner(id, request);
        ApiResponse<LessonPlannerResponse> apiResponse = new ApiResponse<>("Lesson planner updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteLessonPlanner(@PathVariable Long id) {
        lessonPlannerService.deleteLessonPlanner(id);
        ApiResponse<String> apiResponse = new ApiResponse<>("Lesson planner deleted successfully", true, null);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonPlannerResponse>> getLessonPlannerById(@PathVariable Long id) {
        LessonPlannerResponse response = lessonPlannerService.getLessonPlannerById(id);
        ApiResponse<LessonPlannerResponse> apiResponse = new ApiResponse<>("Lesson planner fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LessonPlannerResponse>>> getAllLessonPlanners() {
        List<LessonPlannerResponse> responseList = lessonPlannerService.getAllLessonPlanners();
        ApiResponse<List<LessonPlannerResponse>> apiResponse = new ApiResponse<>("Lesson planners fetched successfully", true, responseList);
        return ResponseEntity.ok(apiResponse);
    }
}
