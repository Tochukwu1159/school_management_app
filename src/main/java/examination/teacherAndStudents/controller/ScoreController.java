package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ScoreRequest;
import examination.teacherAndStudents.entity.Result;
import examination.teacherAndStudents.entity.Score;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.service.ResultService;
import examination.teacherAndStudents.service.ScoreService;
import examination.teacherAndStudents.utils.ScoreType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scores")
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping("/add")
    public ResponseEntity<String> addScore(@RequestBody @Valid ScoreRequest scoreRequest) {
        try {
            scoreService.addScore(scoreRequest);
            return ResponseEntity.ok("Score added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding score: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadScoresFromCsv( @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty. Please upload a valid CSV file.");
        }

        try {
            scoreService.addScoresFromCsv(file);
            return ResponseEntity.status(HttpStatus.OK).body("Scores uploaded and processed successfully.");
        } catch (CustomInternalServerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
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
