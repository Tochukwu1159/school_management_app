package examination.teacherAndStudents.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);



    @PostMapping("/pdf-to-mcq")
    public ResponseEntity<ApiResponse<QuizCreationResponse>> createQuizFromPDF(
            @RequestParam("request") String requestJson,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            QuizCreationRequest request = objectMapper.readValue(requestJson, QuizCreationRequest.class);
            QuizCreationResponse response = quizService.createQuizFromPDF(request, file);
            ApiResponse<QuizCreationResponse> apiResponse = new ApiResponse<>(
                    "Quiz created successfully", true, response
            );
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Failed to process request: {}", e.getMessage());
            ApiResponse<QuizCreationResponse> apiResponse = new ApiResponse<>(
                    "Failed to create quiz: " + e.getMessage(), false, null
            );
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user/questions/{quizId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<QuizQuestionsResponse>> getQuizQuestions(
            @PathVariable Long quizId) {
        QuizQuestionsResponse response = quizService.getQuizQuestions(quizId);
        ApiResponse<QuizQuestionsResponse> apiResponse = new ApiResponse<>(
                "Quiz questions fetched successfully", true, response
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/user/submit/{quizId}")
//    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizSubmissionRequest request
    ) {
        request.setQuizId(quizId);
        QuizSubmissionResponse response = quizService.submitQuiz(request);
        ApiResponse<QuizSubmissionResponse> apiResponse = new ApiResponse<>(
                "Quiz submitted successfully", true, response
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/results/{quizId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<QuizResultsResponse>>> getQuizResults(
            @PathVariable Long quizId,
            Authentication authentication
    ) {
        List<QuizResultsResponse> response = quizService.getQuizResults(quizId, authentication.getName());
        ApiResponse<List<QuizResultsResponse>> apiResponse = new ApiResponse<>(
                "Quiz results fetched successfully", true, response
        );
        return ResponseEntity.ok(apiResponse);
    }
}