package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ResultService;
import examination.teacherAndStudents.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final SessionAverageRepository sessionAverageRepository;

    @Autowired
    public ResultServiceImpl(ScoreService scoreService, UserRepository userRepository, ScoreRepository scoreRepository,
                             ResultRepository resultRepository,
                             PositionRepository positionRepository,
                             ClassLevelRepository classLevelRepository, ClassBlockRepository classBlockRepository, ProfileRepository profileRepository, AcademicSessionRepository academicSessionRepository, StudentTermRepository studentTermRepository, SessionAverageRepository sessionAverageRepository) {
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
        this.sessionAverageRepository = sessionAverageRepository;
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

            AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new NotFoundException("Student academic session not found"));

            StudentTerm studentTerm = studentTermRepository.findById(termId)
                    .orElseThrow(() -> new NotFoundException("Student term not found"));


            // Retrieve the score for the student and subject
            Score score = scoreRepository.findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndStudentTerm(userProfile, studentClass.getId(), subjectName, academicSession, studentTerm);

            if (score == null) {
                throw new NotFoundException("Score not found for the specified criteria");
            }


            // Calculate total marks and grade based on your logic
            double totalMarks = calculateTotalMarks(score.getExamScore(), score.getAssessmentScore());
            String grade = calculateGrade(totalMarks);
            String rating =calculateRating(totalMarks);

            // Check if a result already exists for the student and subject
            Result existingResult = resultRepository.findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndStudentTerm(userProfile, studentClass.getId() ,subjectName,academicSession, studentTerm);

            if (existingResult != null) {
                // Update the existing result
                existingResult.setTotalMarks(totalMarks);
                existingResult.setClassBlock(studentClass);
                existingResult.setGrade(grade);
                existingResult.setAcademicYear(academicSession);
                existingResult.setUserProfile(userProfile);
                existingResult.setStudentTerm(studentTerm);
                resultRepository.save(existingResult);
                return existingResult;
            }
            // Create a new result and save it
            Result result = new Result();
            result.setTotalMarks(totalMarks);
            result.setUserProfile(userProfile);
            result.setClassBlock(studentClass);
            result.setRating(rating);
            result.setAcademicYear(academicSession);
            result.setSubjectName(subjectName);
            result.setStudentTerm(studentTerm);
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
        return examScore + assessmentScore;
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
    public void calculateAverageResult(Long sessionId, Long classLevelId, Long termId) {

        AcademicSession academicYear = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        ClassBlock classBlock = classBlockRepository.findById(classLevelId)
                .orElseThrow(() -> new NotFoundException("Class level not found"));

        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found"));

        // Fetch all results for the specified session, class level, and term
        List<Result> results = resultRepository.findAllByClassBlockAndAcademicYearAndStudentTerm(
                classBlock, academicYear, studentTerm);

        if (results.isEmpty()) {
            throw new NotFoundException("No results found for the specified criteria");
        }

        // Group results by student profile
        Map<Profile, List<Result>> resultsByStudent = results.stream()
                .collect(Collectors.groupingBy(Result::getUserProfile));

        resultsByStudent.forEach((studentProfile, studentResults) -> {
            double totalScore = studentResults.stream()
                    .mapToDouble(Result::getTotalMarks)
                    .sum();

            double averageScore = Math.round((totalScore / studentResults.size()) * 100.0) / 100.0;

            // Fetch or create the position
            Position position = positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                    studentProfile, classBlock, academicYear, studentTerm);

            if (position == null) {
                position = new Position();
                position.setUserProfile(studentProfile);
                position.setClassBlock(classBlock);
                position.setAcademicYear(academicYear);
                position.setStudentTerm(studentTerm);
            }

            // Update and save the position
            position.setAverageScore(averageScore);
            positionRepository.save(position);
        });
    }

    @Transactional
    public void promoteStudents(Long sessionId, Long presentClassId, Long futureSessionId, Long futurePClassId, Long futureFClassId, int cutOff) {

        // Fetch current session, present class, future session, and classes
        AcademicSession currentSession = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        ClassBlock presentClass = classBlockRepository.findById(presentClassId)
                .orElseThrow(() -> new NotFoundException("Present class not found"));

        AcademicSession futureSession = academicSessionRepository.findById(futureSessionId)
                .orElseThrow(() -> new NotFoundException("Future session not found"));

        ClassBlock futurePClass = classBlockRepository.findById(futurePClassId)
                .orElseThrow(() -> new NotFoundException("Promoted class not found"));

        ClassBlock futureFClass = classBlockRepository.findById(futureFClassId)
                .orElseThrow(() -> new NotFoundException("Demoted class not found"));

        // Fetch session averages
        List<SessionAverage> sessionAverages = sessionAverageRepository.findAllByClassBlockAndAcademicYear(
                presentClass, currentSession);

        if (sessionAverages.isEmpty()) {
            throw new NotFoundException("No session averages found for the specified criteria");
        }

        sessionAverages.forEach(sessionAverage -> {
            double averageScore = sessionAverage.getAverageScore();
            Profile studentProfile = sessionAverage.getUserProfile();

            if (averageScore >= cutOff) {
                // Promote student to future promoted class
                studentProfile.setClassBlock(futurePClass);
                profileRepository.save(studentProfile);

                // Update class count
                futurePClass.setNumberOfStudents(futurePClass.getNumberOfStudents() + 1);
                classBlockRepository.save(futurePClass);
            } else {
                // Demote student to future failed class
                studentProfile.setClassBlock(futureFClass);
                profileRepository.save(studentProfile);

                // Update class count
                futureFClass.setNumberOfStudents(futureFClass.getNumberOfStudents() + 1);
                classBlockRepository.save(futureFClass);
            }
        });
    }


    @Override
    public void updateSessionAverage(List<Profile> studentProfiles, ClassBlock classBlock, AcademicSession academicYear) {
        for (Profile studentProfile : studentProfiles) {
            // Fetch the positions for all three terms (first, second, third)
            Position firstTermPosition = positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                    studentProfile, classBlock, academicYear,
                    studentTermRepository.findByNameAndAcademicSession("First Term", academicYear)
            );

            Position secondTermPosition = positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                    studentProfile, classBlock, academicYear,
                    studentTermRepository.findByNameAndAcademicSession("Second Term", academicYear)
            );

            Position thirdTermPosition = positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                    studentProfile, classBlock, academicYear,
                    studentTermRepository.findByNameAndAcademicSession("Third Term", academicYear)
            );

            if (firstTermPosition != null && secondTermPosition != null && thirdTermPosition != null) {
                // Calculate the average score of the three terms
                double averageScore = (firstTermPosition.getAverageScore() +
                        secondTermPosition.getAverageScore() +
                        thirdTermPosition.getAverageScore()) / 3;

                // Check if the SessionAverage already exists for the student, class, and academic year
                SessionAverage sessionAverage = sessionAverageRepository.findByUserProfileAndAcademicYearAndClassBlock(
                        studentProfile, academicYear, classBlock
                );

                if (sessionAverage == null) {
                    // Create a new SessionAverage record if it doesn't exist
                    sessionAverage = new SessionAverage();
                    sessionAverage.setUserProfile(studentProfile);
                    sessionAverage.setAcademicYear(academicYear);
                    sessionAverage.setClassBlock(classBlock);
                    sessionAverage.setFirstTermPosition(firstTermPosition);
                    sessionAverage.setSecondTermPosition(secondTermPosition);
                    sessionAverage.setThirdTermPosition(thirdTermPosition);
                    sessionAverage.setAverageScore(averageScore);
                    sessionAverageRepository.save(sessionAverage);
                } else {
                    // Update the existing SessionAverage record
                    sessionAverage.setFirstTermPosition(firstTermPosition);
                    sessionAverage.setSecondTermPosition(secondTermPosition);
                    sessionAverage.setThirdTermPosition(thirdTermPosition);
                    sessionAverage.setAverageScore(averageScore);
                    sessionAverageRepository.save(sessionAverage);
                }
            }
        }
    }


}
