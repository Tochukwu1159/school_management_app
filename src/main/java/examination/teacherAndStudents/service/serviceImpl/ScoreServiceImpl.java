package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.ScoreRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ResultService;
import examination.teacherAndStudents.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private SubjectRepository subjectRepository;

    @Autowired
    @Lazy
    private ResultService resultService;
    @Autowired
    private ClassLevelRepository classLevelRepository;
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
//    @Autowired
//    private  ResultService resultService;

    public void addScore(ScoreRequest scoreRequest) {
        User student = userRepository.findById(scoreRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Profile studentProfile = profileRepository.findByUser(student)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        ClassBlock studentClass = classBlockRepository.findById(scoreRequest.getClassLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Student class not found"));

        ClassSubject classSubject1 = classSubjectRepository.findById(scoreRequest.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Class Subject not found")); //from the big subject


        Subject subject = subjectRepository.findById(classSubject1.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found")); //from the big subject



        AcademicSession academicSession = academicSessionRepository.findById(scoreRequest.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        StudentTerm studentTerm = studentTermRepository.findById(scoreRequest.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Student term not found"));


        if (!studentTerm.getAcademicSession().equals(academicSession)) {
            throw new IllegalStateException("The selected student term does not belong to the provided academic session.");
        }

        List<String> classSubjects = studentProfile.getClassBlock()
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
        Score existingScore = scoreRepository.findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndStudentTerm(studentProfile, scoreRequest.getClassLevelId(), subject.getName(),academicSession, studentTerm);

        if (existingScore != null) {
            // Update the existing score
            existingScore.setExamScore(scoreRequest.getExamScore());
            existingScore.setAssessmentScore(scoreRequest.getAssessmentScore());
            existingScore.setStudentTerm(studentTerm);
            existingScore.setSubject(classSubject1);
            existingScore.setClassBlock(studentClass);
            scoreRepository.save(existingScore);
        } else {
            // Create a new Score object
            Score score = new Score();
            score.setUserProfile(studentProfile);
            score.setSubjectName(subject.getName());
            score.setExamScore(scoreRequest.getExamScore());
            score.setClassBlock(studentClass);
            score.setSubject(classSubject1);
            score.setAcademicYear(academicSession);
            score.setAssessmentScore(scoreRequest.getAssessmentScore());
            score.setStudentTerm(studentTerm);
            // Save the score
            scoreRepository.save(score);
        }

        // After saving the score, calculate the result using a separate service method
        resultService.calculateResult(scoreRequest.getClassLevelId(), student.getId(), subject.getName(),scoreRequest.getSessionId(), studentTerm.getId());
    }





    public List<Score> getScoresByStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomInternalServerException("Student not found"));
        Profile profile = profileRepository.findById(studentId)
                .orElseThrow(() -> new CustomInternalServerException("Student profile not found"));

        return scoreRepository.findScoreByUserProfile(profile);
    }

    // You can add more methods for updating and deleting scores as needed
}
