package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.ScoreRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ResultService;
import examination.teacherAndStudents.service.ScoreService;
import examination.teacherAndStudents.utils.Roles;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private ResultService resultService;
    @Autowired
    private ClassBlockRepository classBlockRepository;
    @Autowired
    private ClassSubjectRepository classSubjectRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AcademicSessionRepository academicSessionRepository;
    @Autowired
    private StudentTermRepository studentTermRepository;

    @Autowired
    private Validator validator;
    @Autowired
    private SessionClassRepository sessionClassRepository;
//    @Autowired
//    private  ResultService resultService;

    public void addScoresFromCsv(MultipartFile file) {
        List<ScoreRequest> scoreRequests = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip the header
                    continue;
                }

                String[] data = line.split(",");
                if (data.length != 7) {
                    throw new IllegalArgumentException("Invalid CSV format. Each row must have exactly 7 fields.");
                }

                ScoreRequest scoreRequest = ScoreRequest.builder()
                        .studentId(Long.parseLong(data[0].trim()))
                        .classLevelId(Long.parseLong(data[1].trim()))
                        .sessionId(Long.parseLong(data[2].trim()))
                        .termId(Long.parseLong(data[3].trim()))
                        .subjectId(Long.parseLong(data[4].trim()))
                        .assessmentScore(Integer.parseInt(data[5].trim()))
                        .examScore(Integer.parseInt(data[6].trim()))
                        .build();

                // Validate the scoreRequest
                var violations = validator.validate(scoreRequest);
                if (!violations.isEmpty()) {
                    String errorMessage = violations.stream()
                            .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                            .collect(Collectors.joining(", "));
                    throw new IllegalArgumentException("Validation failed: " + errorMessage);
                }

                scoreRequests.add(scoreRequest);
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Failed to process CSV file: " + e.getMessage());
        }

        // Process each score request
        for (ScoreRequest scoreRequest : scoreRequests) {
            try {
                addScore(scoreRequest);
            } catch (Exception e) {
                throw new EntityNotFoundException("Failed to add score for student ID: " + scoreRequest.getStudentId() + " - " + e.getMessage());
//                System.err.println("Failed to add score for student ID: " + scoreRequest.getStudentId() + " - " + e.getMessage());
            }
        }
    }

        public void addScore(ScoreRequest scoreRequest) {
        Optional<User> student = userRepository.findByIdAndRole(scoreRequest.getStudentId(), Roles.STUDENT);
        if (student == null) {
            throw new EntityNotFoundException("Student not found");
        }

        Profile studentProfile = profileRepository.findByUser(student.get())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

            SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(scoreRequest.getSessionId(), scoreRequest.getClassBlockId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));


            ClassSubject classSubject1 = classSubjectRepository.findByIdAndClassBlockId(scoreRequest.getSubjectId(), sessionClass.getClassBlock().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Class Subject not found in the class")); //from the big subject


        Subject subject = classSubject1.getSubject();

        StudentTerm studentTerm = studentTermRepository.findById(scoreRequest.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Student term not found"));


        if (!studentTerm.getAcademicSession().equals(sessionClass.getAcademicSession())) {
            throw new IllegalStateException("The selected student term does not belong to the provided academic session.");
        }

        List<String> classSubjects = studentProfile.getSessionClass().getClassBlock()
                .getSubjects().stream()
                .map(classSubject -> classSubject.getSubject().getName())
                .toList();


        // Check if the provided subject is in the list of subjects for the class level
        if (!classSubjects.contains(classSubject1.getSubject().getName())) {
            throw new EntityNotFoundException("Error adding score: Subject " + classSubject1.getSubject().getName() + " is not part of the student's class level.");
        }

        if (scoreRequest.getExamScore() < 0 || scoreRequest.getExamScore() > 100 ||
                scoreRequest.getAssessmentScore() < 0 || scoreRequest.getAssessmentScore() > 100) {
            throw new EntityNotFoundException("Invalid exam or assessment score");
        }


        // Check if a score already exists for the student and subject
        Score existingScore = scoreRepository.findByUserProfileAndSessionClassIdAndSubjectNameAndAcademicYearAndStudentTerm(studentProfile, scoreRequest.getClassLevelId(), subject.getName(),sessionClass.getAcademicSession(), studentTerm).orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (existingScore != null) {
            // Update the existing score
            existingScore.setExamScore(scoreRequest.getExamScore());
            existingScore.setAssessmentScore(scoreRequest.getAssessmentScore());
            existingScore.setStudentTerm(studentTerm);
            existingScore.setSubject(classSubject1);
            existingScore.setSessionClass(sessionClass);
            scoreRepository.save(existingScore);
        } else {
            // Create a new Score object
            Score score = new Score();
            score.setUserProfile(studentProfile);
            score.setSubjectName(subject.getName());
            score.setExamScore(scoreRequest.getExamScore());
            score.setSessionClass(sessionClass);
            score.setSubject(classSubject1);
            score.setAcademicYear(sessionClass.getAcademicSession());
            score.setAssessmentScore(scoreRequest.getAssessmentScore());
            score.setStudentTerm(studentTerm);
            // Save the score
            scoreRepository.save(score);
        }

        // After saving the score, calculate the result using a separate service method
        resultService.calculateResult(scoreRequest.getClassLevelId(), student.get().getId(), subject.getName(),scoreRequest.getSessionId(), studentTerm.getId());
    }


    public List<Score> getScoresByStudent(Long studentId) {
        Profile profile = profileRepository.findById(studentId)
                .orElseThrow(() -> new CustomInternalServerException("Student profile not found"));

        return scoreRepository.findScoreByUserProfile(profile);
    }

    // You can add more methods for updating and deleting scores as needed
}
