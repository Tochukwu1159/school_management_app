package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.ScoreRequest;
import examination.teacherAndStudents.entity.Score;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    /**
     * Add a score for a student
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addScore(@RequestBody @Valid ScoreRequest scoreRequest) {
        try {
            scoreService.addScore(scoreRequest);
            ApiResponse<String> response = new ApiResponse<>("Score added successfully", true, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>( e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Upload scores from a CSV file
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadScoresFromCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            ApiResponse<String> response = new ApiResponse<>("File is empty. Please upload a valid CSV file.", false, null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            scoreService.addScoresFromCsv(file);
            ApiResponse<String> response = new ApiResponse<>("Scores uploaded and processed successfully.", true, null);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (CustomInternalServerException e) {
            ApiResponse<String> response = new ApiResponse<>("Error processing file: " + e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Unexpected error: " + e.getMessage(), false, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get scores of a student by student ID
     */
    @GetMapping("/getByStudent/{studentId}")
    public ResponseEntity<ApiResponse<List<Score>>> getScoresByStudent(@PathVariable Long studentId) {
        try {
            List<Score> scores = scoreService.getScoresByStudent(studentId);
            ApiResponse<List<Score>> response = new ApiResponse<>("Scores fetched successfully", true, scores);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error fetching scores: " + e.getMessage(), false, null);
            return null;
        }
    }

    // You can add more methods for updating and deleting scores as needed
}
