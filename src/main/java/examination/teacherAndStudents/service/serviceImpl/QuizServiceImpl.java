package examination.teacherAndStudents.service.serviceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.EntityAlreadyExistException;
import examination.teacherAndStudents.repository.ClassSubjectRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.QuizRepository;
import examination.teacherAndStudents.repository.QuizResultRepository;
import examination.teacherAndStudents.service.QuizService;
import examination.teacherAndStudents.utils.PDFTextExtractor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class QuizServiceImpl implements QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private PDFTextExtractor pdfTextExtractor;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Autowired
    private ClassSubjectRepository classSubjectRepository;
    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public QuizCreationResponse createQuizFromPDF(QuizCreationRequest request, MultipartFile file) {
        if (file == null || request.getSubjectId() == null || request.getTitle() == null) {
            throw new IllegalArgumentException("PDF file, subject ID, and title are required");
        }
                String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile teacher = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));


        ClassSubject subject = classSubjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID"));

        try {
            String text = pdfTextExtractor.extractTextFromPDF(file);

            HttpEntity<String> entity = getHttpEntity(request, text);

            ResponseEntity<String> response = restTemplate.exchange(geminiApiUrl, HttpMethod.POST, entity, String.class);
            String fullGeminiResponse = Objects.requireNonNull(response.getBody());
            JsonNode rootNode = objectMapper.readTree(fullGeminiResponse);

            JsonNode textNode = rootNode.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || !textNode.isTextual()) {
                throw new RuntimeException("Could not find the 'text' content containing the quiz questions in Gemini response.");
            }

            String quizJsonString = textNode.asText().replaceAll("```json\n|```", "").trim();

            List<QuizCreationResponse.QuestionDTO> quizQuestions = objectMapper.readValue(
                    quizJsonString, new TypeReference<>() {
                    }
            );

            Quiz quiz = new Quiz();
            quiz.setTeacher(teacher);
            quiz.setSubject(subject);
            quiz.setTitle(request.getTitle());
            quiz.setPdfUrl(file.getOriginalFilename());
            quiz.setQuizTime(request.getQuizTime());
            quiz.setDuration(request.getDuration());
            quiz.setQuestionsPerStudent(request.getQuestionsPerStudent());
            quiz.setQuestions(quizQuestions.stream().map(q -> {
                Quiz.Question question = new Quiz.Question();
                question.setId(java.util.UUID.randomUUID().toString());
                question.setQuestionText(q.getQuestion());
                try {
                    question.setOptions(objectMapper.writeValueAsString(q.getOptions())); // Serialize options to JSON
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialize options: " + e.getMessage());
                }
                question.setCorrectOption(q.getOptions().indexOf(q.getCorrectAnswer()));
                question.setExplanation(q.getExplanation());
                return question;
            }).collect(Collectors.toList()));
            quiz.setStatus(Quiz.QuizStatus.GENERATED);

            quiz = quizRepository.save(quiz);

            QuizCreationResponse responseDTO = new QuizCreationResponse();
            responseDTO.setQuizId(quiz.getId());
            responseDTO.setTitle(quiz.getTitle());
            responseDTO.setSubjectId(quiz.getSubject().getId());
            responseDTO.setQuestions(quizQuestions);

            return responseDTO;
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Failed to generate quiz: " + e.getMessage());
        }
    }

    private @NotNull HttpEntity<String> getHttpEntity(QuizCreationRequest request, String text) {
        String prompt = String.format(
                "Based on the content below, generate %d multiple-choice questions in the following JSON format:\n" +
                        "[{\"question\": \"Sample Question?\", \"options\": [\"A\", \"B\", \"C\", \"D\"], " +
                        "\"correctAnswer\": \"A\", \"explanation\": \"Explanation referencing the content.\"}, ...]\n" +
                        "Content:\n%s",
                request.getNumQuestions(), text
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-goog-api-key", geminiApiKey);
        headers.set("Content-Type", "application/json");
        String requestBody = String.format("{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}", escapeJson(prompt));

        return new HttpEntity<>(requestBody, headers);
    }
    private String escapeJson(String text) {
        return text.replace("\"", "\\\"");
    }

    @Override
    public QuizQuestionsResponse getQuizQuestions(Long quizId) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        if (quiz.getQuizTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(quiz.getQuizTime().plusMinutes(quiz.getDuration()))) {
                throw new IllegalStateException("Quiz has expired and cannot be accessed");
            } else if (now.isBefore(quiz.getQuizTime())) {
                long minutesUntilStart = Duration.between(now, quiz.getQuizTime()).toMinutes();
                throw new IllegalStateException("Quiz not yet available, check back in " + minutesUntilStart + " minutes");
            }
        }

        if (quiz.getQuestions().size() < quiz.getQuestionsPerStudent()) {
            throw new IllegalArgumentException(
                    String.format("Quiz has only %d questions, requested %d", quiz.getQuestions().size(), quiz.getQuestionsPerStudent())
            );
        }

        List<Quiz.Question> shuffledQuestions = new ArrayList<>(quiz.getQuestions());
        Collections.shuffle(shuffledQuestions);
        List<QuizQuestionsResponse.QuestionDTO> selectedQuestions = shuffledQuestions.stream()
                .limit(quiz.getQuestionsPerStudent())
                .map(q -> {
                    QuizQuestionsResponse.QuestionDTO dto = new QuizQuestionsResponse.QuestionDTO();
                    dto.setQuestionId(q.getId());
                    dto.setQuestionText(q.getQuestionText());
                    try {
                        List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<List<String>>() {});
                        dto.setOptions(options); // Deserialize options from JSON
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize options: " + e.getMessage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        QuizQuestionsResponse response = new QuizQuestionsResponse();
        response.setQuizId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setSubjectId(quiz.getSubject().getId());
        response.setQuestions(selectedQuestions);

        return response;
    }


    @Override
    public QuizSubmissionResponse submitQuiz(QuizSubmissionRequest request) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile student = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        QuizResult alreadySubmitted = quizResultRepository.findByQuizIdAndStudentId(request.getQuizId(), student.getId());
        if(alreadySubmitted != null){
            throw new EntityAlreadyExistException("Quiz Already Submitted");
        }


        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        LocalDateTime now = LocalDateTime.now();
        if (quiz.getQuizTime() != null && quiz.getDuration() != null) {
            LocalDateTime quizEndTime = quiz.getQuizTime().plusMinutes(quiz.getDuration());
            if (now.isBefore(quiz.getQuizTime())) {
                throw new IllegalStateException("Quiz has not started. Please check back at " + quiz.getQuizTime());
            } else if (now.isAfter(quizEndTime)) {
                throw new IllegalStateException("Quiz time is over. You cannot submit after " + quizEndTime);
            }
        }


        int score = 0;
        List<QuizSubmissionResponse.FeedbackDTO> feedback = new ArrayList<>();

        for (QuizSubmissionRequest.AnswerDTO answer : request.getAnswers()) {
            Quiz.Question question = quiz.getQuestions().stream()
                    .filter(q -> q.getId().equals(answer.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid question ID: " + answer.getQuestionId()));

            boolean isCorrect = question.getCorrectOption().equals(answer.getSelectedOption());
            if (isCorrect) score++;

            QuizSubmissionResponse.FeedbackDTO feedbackDTO = new QuizSubmissionResponse.FeedbackDTO();
            feedbackDTO.setQuestionId(answer.getQuestionId());
            feedbackDTO.setCorrect(isCorrect);
            feedbackDTO.setExplanation(question.getExplanation());
            feedback.add(feedbackDTO);
        }

        QuizResult result = new QuizResult();
        result.setStudent(student);
        result.setQuiz(quiz);
        result.setScore(score);
        result.setTotal(quiz.getQuestionsPerStudent());
        result.setAnswers(request.getAnswers().stream().map(a -> {
            QuizResult.Answer answerEntity = new QuizResult.Answer();
            answerEntity.setQuestionId(a.getQuestionId());
            answerEntity.setSelectedOption(a.getSelectedOption());
            return answerEntity;
        }).collect(Collectors.toList()));
        result.setFeedback(feedback.stream().map(f -> {
            QuizResult.Feedback feedbackEntity = new QuizResult.Feedback();
            feedbackEntity.setQuestionId(f.getQuestionId());
            feedbackEntity.setCorrect(f.getCorrect());
            feedbackEntity.setExplanation(f.getExplanation());
            return feedbackEntity;
        }).collect(Collectors.toList()));

        quizResultRepository.save(result);

        QuizSubmissionResponse response = new QuizSubmissionResponse();
        response.setResultId(result.getId());
        response.setScore(score);
        response.setFeedback(feedback);

        return response;
    }

    @Override
    public List<QuizResultsResponse> getQuizResults(Long quizId, String teacherId) {
        List<QuizResult> results = quizResultRepository.findByQuizId(quizId);

        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile student = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        return results.stream().map(r -> {
            QuizResultsResponse response = new QuizResultsResponse();
            response.setResultId(r.getId());
            response.setUserId(student.getId());
            response.setScore(r.getScore());
            response.setAnswers(r.getAnswers().stream().map(a -> {
                QuizResultsResponse.AnswerDTO dto = new QuizResultsResponse.AnswerDTO();
                dto.setQuestionId(a.getQuestionId());
                dto.setSelectedOption(a.getSelectedOption());
                return dto;
            }).collect(Collectors.toList()));
            response.setFeedback(r.getFeedback().stream().map(f -> {
                QuizResultsResponse.FeedbackDTO dto = new QuizResultsResponse.FeedbackDTO();
                dto.setQuestionId(f.getQuestionId());
                dto.setCorrect(f.getCorrect());
                dto.setExplanation(f.getExplanation());
                return dto;
            }).collect(Collectors.toList()));
            return response;
        }).collect(Collectors.toList());
    }
}