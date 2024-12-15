package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ScoreRequest;
import examination.teacherAndStudents.entity.Result;
import examination.teacherAndStudents.entity.Score;
import examination.teacherAndStudents.service.ResultService;
import examination.teacherAndStudents.service.ScoreService;
import examination.teacherAndStudents.utils.ScoreType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scores")
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping("/add")
    public ResponseEntity<String> addScore(@RequestBody  ScoreRequest scoreRequest) {
        try {
            scoreService.addScore(scoreRequest);
            return ResponseEntity.ok("Score added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding score: " + e.getMessage());
        }
    }

    @GetMapping("/getByStudent/{studentId}")
    public ResponseEntity<List<Score>> getScoresByStudent(@PathVariable Long studentId) {
        try {
            List<Score> scores = scoreService.getScoresByStudent(studentId);
            return ResponseEntity.ok(scores);
        } catch (Exception e) {
        throw  new RuntimeException("Error fetching scores: " + e.getMessage());
        }
    }

    // You can add more methods for updating and deleting scores as needed
}
