package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.service.GradeRatingService;
import examination.teacherAndStudents.service.serviceImpl.GradeRatingServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grade-ratings")
public class GradeRatingController {

    private final GradeRatingService gradeRatingService;

    public GradeRatingController(GradeRatingServiceImpl gradeRatingService) {
        this.gradeRatingService = gradeRatingService;
    }

    @PostMapping
    public ResponseEntity<?> createGradeRatings(@RequestBody GradeRatingRequestArray requestArray) {
        gradeRatingService.createGradeRatings(requestArray);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<?> updateGradeRatings(@RequestBody GradeRatingRequestArray requestArray) {
        gradeRatingService.updateGradeRatings(requestArray);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/school/{schoolId}")
    public ResponseEntity<?> deleteGradeRatingsBySchool(@PathVariable Long schoolId) {
        gradeRatingService.deleteGradeRatingsBySchool(schoolId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGradeRatingById(
            @PathVariable Long id,
            @RequestParam String type) {
        gradeRatingService.deleteGradeRatingById(id, type);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<GradeRatingResponseArray> getGradeRatingsBySchool(
            @PathVariable Long schoolId) {
        return ResponseEntity.ok(gradeRatingService.getGradeRatingsBySchool(schoolId));
    }
}