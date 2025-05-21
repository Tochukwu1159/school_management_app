package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.ResultSummary;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.templateService.ReportCardService;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.PositionService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PositionServiceImpl implements PositionService {


    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private ClassBlockRepository classBlockRepository;

    @Autowired
    private AcademicSessionRepository academicSessionRepository;
    @Autowired
    private StudentTermRepository studentTermRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private ReportCardService reportCardService;
    @Autowired
    private SessionClassRepository sessionClassRepository;


    @Transactional
    public void updatePositionsForClass(Long classBlockId, Long sessionId, Long termId) {

        // Fetch academic session
        AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + sessionId));

        // Fetch student term
        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException("Student term not found with ID: " + termId));

        // Fetch class block
        ClassBlock classBlock = classBlockRepository.findById(classBlockId)
                .orElseThrow(() -> new ResourceNotFoundException("Class block not found with ID: " + classBlockId));

        SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId, classBlockId).orElseThrow(()->new ResourceNotFoundException("Session class not found"));

        // Fetch all students in the class block
        Set<Profile> students = sessionClass.getProfiles();
        if (students.isEmpty()) {
            throw new IllegalStateException("No students found in the specified class block.");
        }

        // Fetch positions for the class block, academic session, and student term
        List<Position> existingPositions = positionRepository.findAllBySessionClassAndAcademicYearAndStudentTerm(
                sessionClass, academicSession, studentTerm
        );

        // Map positions by user profile for quick lookup
        Map<Profile, Position> positionMap = existingPositions.stream()
                .collect(Collectors.toMap(Position::getUserProfile, Function.identity()));

        // Sort students by average score in descending order
        List<Profile> sortedStudents = students.stream()
                .sorted(Comparator.comparingDouble(student ->
                        positionMap.getOrDefault(student, new Position()).getAverageScore()).reversed())
                .toList();

        // Update or create positions based on the sorted order
        int rank = 1;
        List<Position> updatedPositions = new ArrayList<>();
        for (Profile student : sortedStudents) {
            Position position = positionMap.getOrDefault(student, new Position());
            position.setSessionClass(sessionClass);
            position.setUserProfile(student);
            position.setAcademicYear(academicSession);
            position.setStudentTerm(studentTerm);
            position.setPositionRank(rank++);
            updatedPositions.add(position);
        }

        // Save all updated positions in bulk
        positionRepository.saveAll(updatedPositions);
    }


    @Transactional
    public void updatePositionForSessionClassForJob(Long classBlockId, Long sessionId, Long termId) {
        // Fetch academic session
        AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + sessionId));

        // Fetch the specific student term
        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException("Student term not found with ID: " + termId));

        // Fetch the session class for the given class block and academic session
        SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId, classBlockId)
                .orElseThrow(() -> new ResourceNotFoundException("SessionClass not found for Academic Session ID: " + sessionId + " and Class Block ID: " + classBlockId));

        // Fetch all students in the session class
        Set<Profile> students = sessionClass.getProfiles();
        if (students.isEmpty()) {
            log.info("No students found for SessionClass ID: {}", sessionClass.getId());
            return;
        }

        // Fetch positions for the session class, academic session, and student term
        List<Position> existingPositions = positionRepository.findAllBySessionClassAndAcademicYearAndStudentTerm(
                sessionClass, academicSession, studentTerm
        );

        // Map positions by user profile for quick lookup
        Map<Profile, Position> positionMap = existingPositions.stream()
                .collect(Collectors.toMap(Position::getUserProfile, Function.identity()));

        // Sort students by average score in descending order
        List<Profile> sortedStudents = students.stream()
                .sorted(Comparator.comparingDouble(student ->
                        positionMap.getOrDefault(student, new Position()).getAverageScore()).reversed())
                .toList();

        // Update or create positions based on the sorted order
        int rank = 1;
        List<Position> updatedPositions = new ArrayList<>();
        for (Profile student : sortedStudents) {
            Position position = positionMap.getOrDefault(student, new Position());
            position.setSessionClass(sessionClass);
            position.setUserProfile(student);
            position.setAcademicYear(academicSession);
            position.setStudentTerm(studentTerm);
            position.setPositionRank(rank++);
            updatedPositions.add(position);
        }

        // Save all updated positions in bulk
        positionRepository.saveAll(updatedPositions);
    }


    public void generateResultSummaryPdf(Long studentId, Long classLevelId, Long sessionId, Long term) {
        try {
            // Fetch required data
            AcademicSession session = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + sessionId));

            StudentTerm studentTerm = studentTermRepository.findById(term)
                    .orElseThrow(() -> new ResourceNotFoundException("Student term not found with ID: " + term));

            ClassBlock userClass = classBlockRepository.findById(classLevelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student class not found with ID: " + classLevelId));

            User user = userRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

            Profile userProfile = profileRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found with ID: " + user.getId()));

            // Fetch the session class for the given class block and academic session
            SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId, userClass.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("SessionClass not found for Academic Session "));

            List<Score> scores = scoreRepository.findAllByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                    userProfile, sessionClass, session, studentTerm);

            List<Result> results = resultRepository.findAllByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                   userProfile, sessionClass, session, studentTerm);

            Position position = positionRepository.findByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                    userProfile, sessionClass, session, studentTerm).orElseThrow(()->new ResourceNotFoundException("Position not found"));

            // Map scores to a subject -> score map
            Map<String, Map<String, Object>> scoreMap = scores.stream()
                    .collect(Collectors.toMap(
                            Score::getSubjectName,
                            score -> {
                                Map<String, Object> scoreDetails = new HashMap<>();
                                scoreDetails.put("subject", score.getSubjectName()); // Add subject name
                                scoreDetails.put("examScore", score.getExamScore());
                                scoreDetails.put("assessmentScore", score.getAssessmentScore());
                                return scoreDetails;
                            }
                    ));

            // Create a Result object
            ResultSummary result = new ResultSummary();
            result.setStudentId(studentId);
            result.setClassLevelId(classLevelId);
            result.setSessionId(sessionId);
            result.setTerm(term);
            result.setScores(scoreMap);
            result.setResults(results);
            result.setAverageScore(position.getAverageScore());
            result.setPositionRank(position.getPositionRank());

            // Fetch the school name (assuming it's stored in the user's profile or class)
            String schoolName = session.getSchool().getSchoolName(); // Or userClass.getSchoolName()

            // Use the service to generate the report
            String report = reportCardService.generateReportCard(schoolName, result);
            System.out.println(report); // Or save/return the report as needed
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException("Error generating report: " + e.getMessage(), e);
        }
    }


    public void generateReportCardSummaryJob(Long sessionId) {
        try {
            // Fetch required data
            AcademicSession session = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + sessionId));

            List<StudentTerm> studentTerms = studentTermRepository.findByAcademicSession(session);
            if (studentTerms.isEmpty()) {
                log.warn("No student terms found for Academic Session ID: {}", sessionId);
                return;
            }

            List<SessionClass> sessionClasses = sessionClassRepository.findByAcademicSessionId(sessionId);
            if (sessionClasses.isEmpty()) {
                log.warn("No session classes found for Academic Session ID: {}", sessionId);
                return;
            }

            for (StudentTerm studentTerm : studentTerms) {
                for (SessionClass sessionClass : sessionClasses) {
                    Set<Profile> students = sessionClass.getProfiles();
                    if (students.isEmpty()) {
                        log.info("No students found for SessionClass ID: {}", sessionClass.getId());
                        continue;
                    }

                    for (Profile userProfile : students) {
                        List<Score> scores = scoreRepository.findAllByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                                userProfile, sessionClass, session, studentTerm);

                        List<Result> results = resultRepository.findAllByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                                userProfile, sessionClass, session, studentTerm);

                        Position position = positionRepository.findByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                                        userProfile, sessionClass, session, studentTerm)
                                .orElse(new Position()); // Default to new Position if not found

                        // Map scores to a subject -> score map
                        Map<String, Map<String, Object>> scoreMap = scores.stream()
                                .collect(Collectors.toMap(
                                        Score::getSubjectName,
                                        score -> {
                                            Map<String, Object> scoreDetails = new HashMap<>();
                                            scoreDetails.put("subject", score.getSubjectName());
                                            scoreDetails.put("examScore", score.getExamScore());
                                            scoreDetails.put("assessmentScore", score.getAssessmentScore());
                                            return scoreDetails;
                                        }
                                ));

                        // Create a ResultSummary object
                        ResultSummary result = new ResultSummary();
                        result.setStudentId(userProfile.getUser().getId());
                        result.setSessionId(sessionClass.getId());
                        result.setSessionId(sessionId);
                        result.setTerm(studentTerm.getId());
                        result.setScores(scoreMap);
                        result.setResults(results);
                        result.setAverageScore(position.getAverageScore());
                        result.setPositionRank(position.getPositionRank());

                        // Fetch the school name
                        String schoolName = session.getSchool().getSchoolName();

                        // Use the service to generate the report
                        String report = reportCardService.generateReportCard(schoolName, result);
                        System.out.println(report); // Or save/return the report as needed
                    }
                }
            }
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException("Error generating report: " + e.getMessage(), e);
        }
    }

}
