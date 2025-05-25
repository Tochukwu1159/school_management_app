package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuizService {
    QuizCreationResponse createQuizFromPDF(QuizCreationRequest request, MultipartFile file);
    QuizQuestionsResponse getQuizQuestions(Long quizId, Long subjectId);
    QuizSubmissionResponse submitQuiz(QuizSubmissionRequest request);
    List<QuizResultsResponse> getQuizResults(Long quizId, String teacherId);
}