package examination.teacherAndStudents.service.serviceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.EntityAlreadyExistException;
import examination.teacherAndStudents.repository.ClassSubjectRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.QuizAttemptRepository;
import examination.teacherAndStudents.repository.QuizRepository;
import examination.teacherAndStudents.repository.QuizResultRepository;
import examination.teacherAndStudents.service.QuizService;
import examination.teacherAndStudents.utils.PDFTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizServiceImpl.class);
    private static final int GRACE_PERIOD_MINUTES = 10;
    private static final long EARLY_ACCESS_SECONDS = 30;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

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
    @Transactional
    public QuizCreationResponse createQuizFromPDF(QuizCreationRequest request, MultipartFile file) {
        logger.info("Creating quiz from PDF with title: {}", request.getTitle());
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
            logger.debug("Extracted {} characters from PDF", text.length());

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
                    quizJsonString, new TypeReference<>() {}
            );

            Quiz quiz = new Quiz();
            quiz.setTeacher(teacher);
            quiz.setSubject(subject);
            quiz.setTitle(request.getTitle());
            quiz.setPdfUrl(file.getOriginalFilename());
            quiz.setQuizTime(request.getQuizTime());
            quiz.setDuration(request.getDuration());
            quiz.setSchool(teacher.getUser().getSchool());
            quiz.setQuestionsPerStudent(request.getQuestionsPerStudent());
            quiz.setQuestions(quizQuestions.stream().map(q -> {
                Quiz.Question question = new Quiz.Question();
                question.setId(java.util.UUID.randomUUID().toString());
                question.setQuestionText(q.getQuestion());
                try {
                    question.setOptions(objectMapper.writeValueAsString(q.getOptions()));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialize options: " + e.getMessage());
                }
                question.setCorrectOption(q.getOptions().indexOf(q.getCorrectAnswer()));
                question.setExplanation(q.getExplanation());
                return question;
            }).collect(Collectors.toList()));
            quiz.setStatus(Quiz.QuizStatus.GENERATED);

            quiz = quizRepository.save(quiz);
            logger.info("Quiz saved with ID: {}", quiz.getId());

            QuizCreationResponse responseDTO = new QuizCreationResponse();
            responseDTO.setQuizId(quiz.getId());
            responseDTO.setTitle(quiz.getTitle());
            responseDTO.setSubjectId(quiz.getSubject().getId());
            responseDTO.setQuestions(quizQuestions);

            return responseDTO;
        } catch (IOException | RuntimeException e) {
            logger.error("Failed to generate quiz: {}", e.getMessage());
            throw new RuntimeException("Failed to generate quiz: " + e.getMessage());
        }
    }

    private HttpEntity<String> getHttpEntity(QuizCreationRequest request, String text) {
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
    @Transactional
    public QuizQuestionsResponse getQuizQuestions(Long quizId) {
        logger.info("Fetching quiz questions for quiz ID: {}", quizId);

        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile student = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        Quiz quiz = quizRepository.findByIdAndSchoolId(quizId, student.getUser().getSchool().getId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        // Check quiz availability
        if (quiz.getQuizTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime quizTimeWithEarlyAccess = quiz.getQuizTime().minusSeconds(EARLY_ACCESS_SECONDS);
            if (now.isBefore(quizTimeWithEarlyAccess)) {
                Duration timeUntilStart = Duration.between(now, quiz.getQuizTime());
                long secondsUntilStart = timeUntilStart.getSeconds();
                String formattedQuizTime = quiz.getQuizTime().format(DateTimeFormatter.ofPattern("h:mm a"));
                if (secondsUntilStart <= 60) {
                    throw new BadRequestException("Quiz starts in less than a minute at " + formattedQuizTime);
                } else {
                    long minutesUntilStart = (secondsUntilStart + 59) / 60;
                    throw new BadRequestException("Quiz not yet available, check back in " + minutesUntilStart + " minute" + "s" + " at " + formattedQuizTime);
                }
            }
        }
        // Check for existing quiz attempt
        QuizAttempt attempt = quizAttemptRepository.findByQuizIdAndStudentId(quizId, student.getId());
        List<QuizQuestionsResponse.QuestionDTO> selectedQuestions;

        if (attempt != null) {
            logger.info("Found existing quiz attempt for student {} and quiz {}", student.getId(), quizId);
            if (attempt.isCompleted()) {
                throw new IllegalStateException("Quiz already completed");
            }
            // Use existing assigned questions
            selectedQuestions = attempt.getAssignedQuestions().stream()
                    .map(q -> {
                        QuizQuestionsResponse.QuestionDTO dto = new QuizQuestionsResponse.QuestionDTO();
                        dto.setQuestionId(q.getQuestionId());
                        dto.setQuestionText(q.getQuestionText());
                        try {
                            List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<>() {});
                            dto.setOptions(options);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to deserialize options: " + e.getMessage());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());
        } else {
            // Validate available questions
            if (quiz.getQuestions().size() < quiz.getQuestionsPerStudent()) {
                throw new IllegalArgumentException(
                        String.format("Quiz has only %d questions, requested %d", quiz.getQuestions().size(), quiz.getQuestionsPerStudent())
                );
            }

            // Assign new questions
            List<Quiz.Question> shuffledQuestions = new ArrayList<>(quiz.getQuestions());
            Collections.shuffle(shuffledQuestions);
            selectedQuestions = shuffledQuestions.stream()
                    .limit(quiz.getQuestionsPerStudent())
                    .map(q -> {
                        QuizQuestionsResponse.QuestionDTO dto = new QuizQuestionsResponse.QuestionDTO();
                        dto.setQuestionId(q.getId());
                        dto.setQuestionText(q.getQuestionText());
                        try {
                            List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<>() {});
                            dto.setOptions(options);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to deserialize options: " + e.getMessage());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());

            // Save quiz attempt
            QuizAttempt newAttempt = new QuizAttempt();
            newAttempt.setStudent(student);
            newAttempt.setQuiz(quiz);
            newAttempt.setStartTime(LocalDateTime.now());
            newAttempt.setSubmissionDeadline(newAttempt.getStartTime()
                    .plusMinutes(quiz.getDuration() != null ? quiz.getDuration() : 0)
                    .plusMinutes(GRACE_PERIOD_MINUTES));
            newAttempt.setAssignedQuestions(selectedQuestions.stream()
                    .map(dto -> {
                        QuizAttempt.AssignedQuestion aq = new QuizAttempt.AssignedQuestion();
                        aq.setQuestionId(dto.getQuestionId());
                        aq.setQuestionText(dto.getQuestionText());
                        try {
                            aq.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to serialize options: " + e.getMessage());
                        }
                        return aq;
                    })
                    .collect(Collectors.toList()));
            quizAttemptRepository.save(newAttempt);
            logger.info("Created new quiz attempt for student {} and quiz {}", student.getId(), quizId);
        }

        QuizQuestionsResponse response = new QuizQuestionsResponse();
        response.setQuizId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setSubjectId(quiz.getSubject().getId());
        response.setQuestions(selectedQuestions);
        response.setSubmissionDeadline(attempt != null ? attempt.getSubmissionDeadline() :
                LocalDateTime.now().plusMinutes(quiz.getDuration() != null ? quiz.getDuration() : 0)
                        .plusMinutes(GRACE_PERIOD_MINUTES));

        return response;
    }

    @Override
    @Transactional
    public QuizSubmissionResponse submitQuiz(QuizSubmissionRequest request) {
        logger.info("Submitting quiz for quiz ID: {}", request.getQuizId());

        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile student = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        Quiz quiz = quizRepository.findByIdAndSchoolId(request.getQuizId(), student.getUser().getSchool().getId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        // Check for existing submission
        QuizResult alreadySubmitted = quizResultRepository.findByQuizIdAndStudentId(request.getQuizId(), student.getId());
        if (alreadySubmitted != null) {
            throw new EntityAlreadyExistException("Quiz already submitted");
        }

        // Check quiz attempt and deadline
        QuizAttempt attempt = quizAttemptRepository.findByQuizIdAndStudentId(request.getQuizId(), student.getId());
        if (attempt == null) {
            throw new IllegalStateException("No quiz attempt found. Please start the quiz first.");
        }
        if (attempt.isCompleted()) {
            throw new IllegalStateException("Quiz already completed");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(attempt.getSubmissionDeadline())) {
            throw new IllegalStateException("Submission deadline has passed: " + attempt.getSubmissionDeadline());
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

        // Save quiz result
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

        // Mark attempt as completed
        attempt.setCompleted(true);
        quizAttemptRepository.save(attempt);
        logger.info("Quiz submitted for student {} and quiz {}", student.getId(), quiz.getId());

        QuizSubmissionResponse response = new QuizSubmissionResponse();
        response.setResultId(result.getId());
        response.setScore(score);
        response.setFeedback(feedback);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResultsResponse> getQuizResults(Long quizId, String teacherId) {
        logger.info("Fetching quiz results for quiz ID: {}", quizId);

        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile teacher = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        List<QuizResult> results = quizResultRepository.findByQuizId(quizId);
        return results.stream().map(r -> {
            QuizResultsResponse response = new QuizResultsResponse();
            response.setResultId(r.getId());
            response.setUserId(r.getStudent().getId());
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