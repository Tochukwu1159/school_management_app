package examination.teacherAndStudents.controller;

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
    public ResponseEntity<LessonPlannerResponse> createLessonPlanner(@RequestBody LessonPlannerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonPlannerService.createLessonPlanner(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonPlannerResponse> updateLessonPlanner(@PathVariable Long id, @RequestBody LessonPlannerRequest request) {
        return ResponseEntity.ok(lessonPlannerService.updateLessonPlanner(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLessonPlanner(@PathVariable Long id) {
        lessonPlannerService.deleteLessonPlanner(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonPlannerResponse> getLessonPlannerById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonPlannerService.getLessonPlannerById(id));
    }

    @GetMapping
    public ResponseEntity<List<LessonPlannerResponse>> getAllLessonPlanners() {
        return ResponseEntity.ok(lessonPlannerService.getAllLessonPlanners());
    }
}

