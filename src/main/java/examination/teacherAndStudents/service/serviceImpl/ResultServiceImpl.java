package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ResultService;
import examination.teacherAndStudents.service.ScoreService;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ResultServiceImpl implements ResultService {

    private final ScoreService scoreService;
    private final UserRepository userRepository;
    private final ScoreRepository scoreRepository;
    private final ResultRepository resultRepository;
    private final PositionRepository positionRepository;
    private final ClassLevelRepository classLevelRepository;
    private final ClassBlockRepository classBlockRepository;
    private final ProfileRepository profileRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;

    @Autowired
    public ResultServiceImpl(ScoreService scoreService, UserRepository userRepository, ScoreRepository scoreRepository,
                             ResultRepository resultRepository,
                             PositionRepository positionRepository,
                             ClassLevelRepository classLevelRepository, ClassBlockRepository classBlockRepository, ProfileRepository profileRepository, AcademicSessionRepository academicSessionRepository, StudentTermRepository studentTermRepository) {
        this.scoreService = scoreService;
        this.userRepository = userRepository;
        this.scoreRepository = scoreRepository;
        this.resultRepository = resultRepository;
        this.positionRepository = positionRepository;
        this.classLevelRepository = classLevelRepository;
        this.classBlockRepository = classBlockRepository;
        this.profileRepository = profileRepository;
        this.academicSessionRepository = academicSessionRepository;
        this.studentTermRepository = studentTermRepository;
    }


    @Override
    public Result calculateResult(Long classLevelId, Long studentId, String subjectName,Long sessionId, Long termId) {
        try {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Student not found"));

            Profile userProfile = profileRepository.findByUser(student)
                    .orElseThrow(() -> new NotFoundException("Student profile not found"));

            ClassBlock studentClass = classBlockRepository.findById(classLevelId)
                    .orElseThrow(() -> new NotFoundException("Student class not found"));

            ClassLevel generalClass = classLevelRepository.findById(classLevelId)
                    .orElseThrow(() -> new NotFoundException("Student class not found in class level"));

            AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new NotFoundException("Student academic session not found"));
            Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(termId);


            // Retrieve the score for the student and subject
            Score score = scoreRepository.findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndStudentTerm(userProfile, studentClass.getId(), subjectName, academicSession, studentTerm.get());

            // Calculate total marks and grade based on your logic
            double totalMarks = calculateTotalMarks(score.getExamScore(), score.getAssessmentScore());
            String grade = calculateGrade(totalMarks);
            String rating =calculateRating(totalMarks);

            // Check if a result already exists for the student and subject
            Result existingResult = resultRepository.findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndStudentTerm(userProfile, studentClass.getId() ,subjectName,academicSession, studentTerm.get());

            if (existingResult != null) {
                // Update the existing result
                existingResult.setTotalMarks(totalMarks);
                existingResult.setClassBlock(studentClass);
                existingResult.setGrade(grade);
                existingResult.setUserProfile(userProfile);
                existingResult.setStudentTerm(studentTerm.get());
                resultRepository.save(existingResult);
                return existingResult;
            }
            // Create a new result and save it
            Result result = new Result();
            result.setTotalMarks(totalMarks);
            result.setUserProfile(userProfile);
            result.setClassBlock(studentClass);
            result.setRating(rating);
            result.setSubjectName(subjectName);
            result.setStudentTerm(studentTerm.get());
            result.setGrade(grade);
            resultRepository.save(result);

            return result;
        } catch (NotFoundException e) {
            throw new CustomInternalServerException("Error calculating result: " + e.getMessage());
        } catch (Exception e) {
            throw new CustomInternalServerException("An unexpected error occurred: " + e.getMessage());
        }
    }

    private double calculateTotalMarks(int examScore, int assessmentScore) {
        double quizAssignmentMarks = assessmentScore; // Assuming assessmentScore is equivalent to quiz/assignment marks
        double examMarks = examScore;

        return quizAssignmentMarks + examMarks;
    }

    private String calculateGrade(double totalMarks) {
        if (totalMarks >= 90) {
            return "A";
        } else if (totalMarks >= 80) {
            return "B";
        } else if (totalMarks >= 70) {
            return "C";
        } else if (totalMarks >= 60) {
            return "D";
        } else {
            return "F";
        }
    }

    private String calculateRating(double totalMarks) {
        if (totalMarks >= 90) {
            return "Excellent";
        } else if (totalMarks >= 80) {
            return "Good Performance";
        } else if (totalMarks >= 70) {
            return "Average Performance";
        } else if (totalMarks >= 60) {
            return "Below Average Performance";
        } else {
            return "Unsatisfactory";
        }
    }

    @Transactional
    public void calculateAverageResult(Long userId, Long classLevelId, Long sessionId, Long term) {

        User student = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        Profile userProfile = profileRepository.findByUser(student)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        ClassBlock studentClass = classBlockRepository.findById(classLevelId)
                .orElseThrow(() -> new NotFoundException("Student class not found"));

        AcademicSession acdemicYear  = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Student sesion class not found"));
        Optional<examination.teacherAndStudents.entity.StudentTerm> studentTerm = studentTermRepository.findById(term);


        ClassLevel generalClass = classLevelRepository.findById(classLevelId)
                .orElseThrow(() -> new NotFoundException("Student class Level not found"));
        // Fetch scores for the user
        List<Result> results = resultRepository.findAllByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(userProfile,studentClass, acdemicYear, studentTerm.get());

        // Calculate average score
        double totalScore = results.stream()
                .mapToDouble(Result::getTotalMarks)
                .sum();
        double score = totalScore / results.size();
        double averageScore = Math.round(score * 100.0) / 100.0;

        // Fetch or create the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));


        // Fetch the existing position or create a new one
        Position position = positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(userProfile, studentClass,acdemicYear, studentTerm.get());
        if (position == null) {
            // Create a new position
            position = new Position();
            position.setUserProfile(userProfile);
            position.setStudentTerm(studentTerm.get());
            position.setClassBlock(studentClass);
            position.setAverageScore(averageScore);
            positionRepository.save(position);
        } else {
            // Update the existing position with the new average score
            position.setAverageScore(averageScore);
            positionRepository.save(position);
        }
    }




}
