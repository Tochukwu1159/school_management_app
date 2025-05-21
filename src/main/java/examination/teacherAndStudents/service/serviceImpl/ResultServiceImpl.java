package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.GradeRatingPair;
import examination.teacherAndStudents.dto.StatisticsReport;
import examination.teacherAndStudents.dto.TermStatisticsReport;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.*;
import examination.teacherAndStudents.utils.SessionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    private final GradeRatingService gradeRatingService;
    private final SessionClassRepository sessionClassRepository;


    @Autowired
    public ResultServiceImpl(ScoreService scoreService, UserRepository userRepository, ScoreRepository scoreRepository,
                             ResultRepository resultRepository,
                             PositionRepository positionRepository,
                             ClassLevelRepository classLevelRepository, ClassBlockRepository classBlockRepository, ProfileRepository profileRepository, AcademicSessionRepository academicSessionRepository, StudentTermRepository studentTermRepository, SessionAverageRepository sessionAverageRepository, GradeRatingService gradeRatingService, SessionClassRepository sessionClassRepository) {
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
        this.gradeRatingService = gradeRatingService;
        this.sessionClassRepository = sessionClassRepository;
    }


    @Override
    public Result calculateResult(Long classLevelId, Long studentId, String subjectName,Long sessionId, Long termId) {
        try {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Student not found"));

            Profile userProfile = profileRepository.findByUser(student)
                    .orElseThrow(() -> new NotFoundException("Student profile not found"));

            SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId, classLevelId)
                    .orElseThrow(() -> new NotFoundException("Student academic session not found"));

            StudentTerm studentTerm = studentTermRepository.findById(termId)
                    .orElseThrow(() -> new NotFoundException("Student term not found"));


            // Retrieve the score for the student and subject
            Score score = scoreRepository.findByUserProfileAndSessionClassIdAndSubjectNameAndAcademicYearAndStudentTerm(userProfile, sessionClass.getId(), subjectName, sessionClass.getAcademicSession(), studentTerm).orElseThrow(() -> new NotFoundException("Student score not found"));

            if (score == null) {
                throw new NotFoundException("Score not found for the specified criteria");
            }

            // Retrieve school information
            School school = student.getSchool();

// Calculate total marks based on your logic
            double totalMarks = calculateTotalMarks(score.getExamScore(), score.getAssessmentScore());

// Fetch both grade and rating in a single call
            GradeRatingPair gradeRating = gradeRatingService.calculateGradeAndRating(school, totalMarks);

// Now you can access both grade and rating from the pair:
            String grade = gradeRating.getGrade();
            String rating = gradeRating.getRating();



            // Check if a result already exists for the student and subject
            Result existingResult = resultRepository.findByUserProfileAndSessionClassIdAndSubjectNameAndAcademicYearAndStudentTerm(userProfile, sessionClass.getId() ,subjectName,sessionClass.getAcademicSession(), studentTerm).orElseThrow(() -> new NotFoundException("Result not found"));

            if (existingResult != null) {
                // Update the existing result
                existingResult.setTotalMarks(totalMarks);
                existingResult.setGrade(grade);
                existingResult.setRating(rating);
                resultRepository.save(existingResult);
                return existingResult;
            }
            // Create a new result and save it
            Result result = new Result();
            result.setTotalMarks(totalMarks);
            result.setUserProfile(userProfile);
            result.setSessionClass(sessionClass);
            result.setAcademicYear(sessionClass.getAcademicSession());
            result.setStudentTerm(studentTerm);
            result.setSubjectName(subjectName);
            result.setGrade(grade);
            result.setRating(rating);
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


@Transactional
    public void calculateAverageResult(Long sessionId, Long classLevelId, Long termId) {
        AcademicSession academicYear = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        ClassBlock classBlock = classBlockRepository.findById(classLevelId)
                .orElseThrow(() -> new NotFoundException("Class level not found"));

    SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId,classBlock.getId())
            .orElseThrow(() -> new NotFoundException("Session Class not found"));

        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found"));

        // Fetch all results for the specified session, class level, and term
        List<Result> results = resultRepository.findAllBySessionClassAndAcademicYearAndStudentTerm(
                sessionClass, academicYear, studentTerm);

        if (results.isEmpty()) {
            throw new NotFoundException("No results found for the specified criteria");
        }

        // Group results by student profile
        Map<Profile, List<Result>> resultsByStudent = results.stream()
                .collect(Collectors.groupingBy(Result::getUserProfile));

        // Pre-fetch existing positions
        Map<Profile, Position> existingPositions = positionRepository.findAllBySessionClassAndAcademicYearAndStudentTerm(
                        sessionClass, academicYear, studentTerm).stream()
                .collect(Collectors.toMap(Position::getUserProfile, Function.identity()));

        // Use a list to collect positions to batch save
        List<Position> positionsToSave = new ArrayList<>();

        resultsByStudent.forEach((studentProfile, studentResults) -> {
            double totalScore = studentResults.stream()
                    .mapToDouble(Result::getTotalMarks)
                    .sum();
            double averageScore = Math.round((totalScore / studentResults.size()) * 100.0) / 100.0;

            // Fetch existing position or create a new one
            Position position = existingPositions.getOrDefault(studentProfile, new Position());
            position.setUserProfile(studentProfile);
            position.setSessionClass(sessionClass);
            position.setAcademicYear(academicYear);
            position.setStudentTerm(studentTerm);
            position.setAverageScore(averageScore);

            positionsToSave.add(position);
        });

        // Batch save all positions
        positionRepository.saveAll(positionsToSave);
    }

    @Transactional
    public void calculateAverageResultJob(Long sessionId, Long termId) {
        // Fetch academic session
        AcademicSession academicYear = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found with ID: " + sessionId));

        // Fetch student term
        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found with ID: " + termId));

        // Fetch all session classes for the academic session
        List<SessionClass> sessionClasses = sessionClassRepository.findByAcademicSessionId(sessionId);
        if (sessionClasses.isEmpty()) {
            log.warn("No session classes found for Academic Session ID: {}", sessionId);
            throw new NotFoundException("No session classes found for the specified session ID: " + sessionId);
        }

        // Process each session class
        for (SessionClass sessionClass : sessionClasses) {
            // Fetch all results for the specified session class, academic year, and term
            List<Result> results = resultRepository.findAllBySessionClassAndAcademicYearAndStudentTerm(
                    sessionClass, academicYear, studentTerm);

            if (results.isEmpty()) {
                log.info("No results found for SessionClass ID: {}, Academic Session ID: {}, Term ID: {}",
                        sessionClass.getId(), sessionId, termId);
                continue; // Skip if no results for this session class
            }

            // Group results by student profile
            Map<Profile, List<Result>> resultsByStudent = results.stream()
                    .collect(Collectors.groupingBy(Result::getUserProfile));

            // Pre-fetch existing positions for this session class
            Map<Profile, Position> existingPositions = positionRepository.findAllBySessionClassAndAcademicYearAndStudentTerm(
                            sessionClass, academicYear, studentTerm).stream()
                    .collect(Collectors.toMap(Position::getUserProfile, Function.identity()));

            // Use a list to collect positions to batch save
            List<Position> positionsToSave = new ArrayList<>();

            resultsByStudent.forEach((studentProfile, studentResults) -> {
                double totalScore = studentResults.stream()
                        .mapToDouble(Result::getTotalMarks)
                        .sum();
                double averageScore = Math.round((totalScore / studentResults.size()) * 100.0) / 100.0;

                // Fetch existing position or create a new one
                Position position = existingPositions.getOrDefault(studentProfile, new Position());
                position.setUserProfile(studentProfile);
                position.setSessionClass(sessionClass);
                position.setAcademicYear(academicYear);
                position.setStudentTerm(studentTerm);
                position.setAverageScore(averageScore);

                positionsToSave.add(position);
            });

            // Batch save all positions for this session class
            positionRepository.saveAll(positionsToSave);
        }
    }

//    @Transactional
//    public void calculateAverageResultJob(Long sessionId, Long termId) {
//        AcademicSession academicYear = academicSessionRepository.findById(sessionId)
//                .orElseThrow(() -> new NotFoundException("Session not found"));
//
//        StudentTerm studentTerm = studentTermRepository.findById(termId)
//                .orElseThrow(() -> new NotFoundException("Term not found"));
//
//        List<ClassBlock> classBlocks = classBlockRepository.findByClassLevelAcademicYear(academicYear);
//
//        for (ClassBlock classBlock : classBlocks) {
//            int page = 0;
//            int pageSize = 1000; // Adjust based on memory constraints
//            boolean hasMoreResults = true;
//
//            while (hasMoreResults) {
//                // Fetch results in pages
//                Page<Result> resultPage = resultRepository.findAllByClassBlockAndAcademicYearAndStudentTerm(
//                        classBlock, academicYear, studentTerm, PageRequest.of(page, pageSize));
//
//                List<Result> results = resultPage.getContent();
//                if (results.isEmpty()) {
//                    hasMoreResults = false;
//                    continue;
//                }
//
//                // Group results by student (for this chunk)
//                Map<Profile, List<Result>> resultsByStudent = results.stream()
//                        .collect(Collectors.groupingBy(Result::getUserProfile));
//
//                // Fetch positions only for students in this chunk (avoid loading all)
//                Set<Long> studentIds = resultsByStudent.keySet().stream()
//                        .map(Profile::getId)
//                        .collect(Collectors.toSet());
//
//                Map<Profile, Position> existingPositions = positionRepository
//                        .findByClassBlockAndAcademicYearAndStudentTermAndUserProfileIn(
//                                classBlock, academicYear, studentTerm, studentIds)
//                        .stream()
//                        .collect(Collectors.toMap(Position::getUserProfile, Function.identity()));
//
//                // Process & save in smaller batches
//                List<Position> positionsToSave = new ArrayList<>();
//                resultsByStudent.forEach((student, studentResults) -> {
//                    double avgScore = studentResults.stream()
//                            .mapToDouble(Result::getTotalMarks)
//                            .average()
//                            .orElse(0.0);
//                    avgScore = Math.round(avgScore * 100.0) / 100.0;
//
//                    Position position = existingPositions.getOrDefault(student, new Position());
//                    position.setUserProfile(student);
//                    position.setClassBlock(classBlock);
//                    position.setAcademicYear(academicYear);
//                    position.setStudentTerm(studentTerm);
//                    position.setAverageScore(avgScore);
//
//                    positionsToSave.add(position);
//                });
//
//                positionRepository.saveAll(positionsToSave);
//                page++;
//            }
//        }
//    }

    @Transactional
    public void promoteStudents(Long sessionId, Long presentClassId, Long futureSessionId, Long futurePClassId, Long futureFClassId, int cutOff) {
        try {
            // Fetch the required entities
            SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId,presentClassId)
                    .orElseThrow(() -> new NotFoundException("Session class  not found"));
            SessionClass futurePSessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(futureSessionId,futurePClassId)
                    .orElseThrow(() -> new NotFoundException("Session class  not found"));

            SessionClass futureFSessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(futureSessionId,futureFClassId)
                    .orElseThrow(() -> new NotFoundException("Session class  not found"));


            // Fetch session averages for students in the current session and class
            List<SessionAverage> sessionAverages = sessionAverageRepository.findAllBySessionClassAndAcademicYear(sessionClass, sessionClass.getAcademicSession());

            if (sessionAverages.isEmpty()) {
                throw new NotFoundException("No session averages found for present class ID: " + presentClassId + " in session ID: " + sessionId);
            }

            int promotedCount = 0;
            int demotedCount = 0;

            for (SessionAverage sessionAverage : sessionAverages) {
                double averageScore = sessionAverage.getAverageScore();
                Profile studentProfile = sessionAverage.getUserProfile();

                if (averageScore >= cutOff) {
                    // Promote student
                    studentProfile.setSessionClass(futurePSessionClass);
                    profileRepository.save(studentProfile);
                    promotedCount++;
                } else {
                    // Demote student
                    studentProfile.setSessionClass(futureFSessionClass);
                    profileRepository.save(studentProfile);
                    demotedCount++;
                }
            }

            // Update class student counts in bulk
            futurePSessionClass.setNumberOfProfiles(futurePSessionClass.getNumberOfProfiles() + promotedCount);
            futureFSessionClass.setNumberOfProfiles(futureFSessionClass.getNumberOfProfiles() + demotedCount);
            sessionClassRepository.save(futurePSessionClass);
            sessionClassRepository.save(futureFSessionClass);

        } catch (NotFoundException e) {
            // Re-throw custom exceptions for clarity
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            throw new RuntimeException("An error occurred during student promotion: " + e.getMessage(), e);
        }
    }



    @Override
    public void updateSessionAverage(Set<Profile> studentProfiles, SessionClass sessionClass) {
        for (Profile studentProfile : studentProfiles) {
            // Fetch the positions for all three terms (first, second, third)
            Position firstTermPosition = positionRepository.findByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                    studentProfile, sessionClass, sessionClass.getAcademicSession(),
                    studentTermRepository.findByNameAndAcademicSession("First Term", sessionClass.getAcademicSession())
            ).orElseThrow(() -> new NotFoundException("Student profile not found "));

            Position secondTermPosition = positionRepository.findByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                    studentProfile, sessionClass, sessionClass.getAcademicSession(),
                    studentTermRepository.findByNameAndAcademicSession("Second Term", sessionClass.getAcademicSession())
            ).orElseThrow(() -> new NotFoundException("Student profile not found "));

            Position thirdTermPosition = positionRepository.findByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                    studentProfile, sessionClass, sessionClass.getAcademicSession(),
                    studentTermRepository.findByNameAndAcademicSession("Third Term", sessionClass.getAcademicSession())
            ).orElseThrow(() -> new NotFoundException("Student profile not found "));

            if (firstTermPosition != null && secondTermPosition != null && thirdTermPosition != null) {
                // Calculate the average score of the three terms
                double averageScore = (firstTermPosition.getAverageScore() +
                        secondTermPosition.getAverageScore() +
                        thirdTermPosition.getAverageScore()) / 3;

                // Check if the SessionAverage already exists for the student, class, and academic year
                SessionAverage sessionAverage = sessionAverageRepository.findByUserProfileAndAcademicYearAndSessionClass(
                        studentProfile, sessionClass.getAcademicSession(), sessionClass
                );

                if (sessionAverage == null) {
                    // Create a new SessionAverage record if it doesn't exist
                    sessionAverage = new SessionAverage();
                    sessionAverage.setUserProfile(studentProfile);
                    sessionAverage.setAcademicYear(sessionClass.getAcademicSession());
                    sessionAverage.setSessionClass(sessionClass);
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

    @Override
    @Transactional
    public void updateSessionAverageForJob(AcademicSession academicYear) {
        // Get all session classes in the academic session
        List<SessionClass> sessionClasses = sessionClassRepository.findByAcademicSessionId(academicYear.getId());
        if (sessionClasses.isEmpty()) {
            log.warn("No session classes found for Academic Session ID: {}", academicYear.getId());
            return;
        }

        for (SessionClass sessionClass : sessionClasses) {
            // Get all student profiles in the session class
            Set<Profile> studentProfiles = sessionClass.getProfiles();
            if (studentProfiles.isEmpty()) {
                log.info("No student profiles found for SessionClass ID: {}", sessionClass.getId());
                continue;
            }

            for (Profile studentProfile : studentProfiles) {
                // Fetch the student terms
                StudentTerm firstTerm = studentTermRepository.findByNameAndAcademicSession("First Term", academicYear);
                StudentTerm secondTerm = studentTermRepository.findByNameAndAcademicSession("Second Term", academicYear);
                StudentTerm thirdTerm = studentTermRepository.findByNameAndAcademicSession("Third Term", academicYear);

                if (firstTerm == null || secondTerm == null || thirdTerm == null) {
                    log.warn("One or more terms (First, Second, Third) not found for Academic Session ID: {}", academicYear.getId());
                    continue;
                }

                // Fetch the positions for all three terms
                Optional<Position> firstTermPosition = positionRepository.findByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                        studentProfile, sessionClass, academicYear, firstTerm);

                Optional<Position> secondTermPosition = positionRepository.findByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                        studentProfile, sessionClass, academicYear, secondTerm);

                Optional<Position> thirdTermPosition = positionRepository.findByUserProfileAndSessionClassAndAcademicYearAndStudentTerm(
                        studentProfile, sessionClass, academicYear, thirdTerm);

                if (firstTermPosition.isPresent() && secondTermPosition.isPresent() && thirdTermPosition.isPresent()) {
                    // Calculate the average score of the three terms
                    double averageScore = (firstTermPosition.get().getAverageScore() +
                            secondTermPosition.get().getAverageScore() +
                            thirdTermPosition.get().getAverageScore()) / 3.0;
                    averageScore = Math.round(averageScore * 100.0) / 100.0; // Round to 2 decimal places

                    // Check if the SessionAverage already exists
                    SessionAverage sessionAverage = sessionAverageRepository.findByUserProfileAndAcademicYearAndSessionClass(
                            studentProfile, academicYear, sessionClass);

                    if (sessionAverage == null) {
                        // Create a new SessionAverage record
                        sessionAverage = SessionAverage.builder()
                                .userProfile(studentProfile)
                                .academicYear(academicYear)
                                .sessionClass(sessionClass)
                                .firstTermPosition(firstTermPosition.get())
                                .secondTermPosition(secondTermPosition.get())
                                .thirdTermPosition(thirdTermPosition.get())
                                .averageScore(averageScore)
                                .build();
                    } else {
                        // Update existing SessionAverage record
                        sessionAverage.setFirstTermPosition(firstTermPosition.get());
                        sessionAverage.setSecondTermPosition(secondTermPosition.get());
                        sessionAverage.setThirdTermPosition(thirdTermPosition.get());
                        sessionAverage.setAverageScore(averageScore);
                    }
                    sessionAverageRepository.save(sessionAverage);
                } else {
                    log.info("Skipping SessionAverage for Profile ID: {} in SessionClass ID: {} due to missing position(s)",
                            studentProfile.getId(), sessionClass.getId());
                }
            }
        }
    }



    @Transactional(readOnly = true)
    public StatisticsReport calculateSessionStatistics(Long sessionId, Long classLevelId) {
        SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId,classLevelId)
                .orElseThrow(() -> new NotFoundException("Session class  not found"));

        // Fetch all session averages for the given session and class
        List<SessionAverage> sessionAverages = sessionAverageRepository.findAllByAcademicYearAndSessionClass(sessionClass.getAcademicSession(), sessionClass);

        if (sessionAverages.isEmpty()) {
            throw new NotFoundException("No average scores found for the specified session and class");
        }

        // Extract average scores
        List<Double> averageScores = sessionAverages.stream()
                .map(SessionAverage::getAverageScore)
                .collect(Collectors.toList());

        // Define updated score ranges
        StatisticsReport.ScoreRange[] ranges = {
                new StatisticsReport.ScoreRange(90, 101, "90-100"), // 101 to include 100
                new StatisticsReport.ScoreRange(80, 90, "80-89"),
                new StatisticsReport.ScoreRange(70, 80, "70-79"),
                new StatisticsReport.ScoreRange(60, 70, "60-69"),
                new StatisticsReport.ScoreRange(50, 60, "50-59"),
                new StatisticsReport.ScoreRange(40, 50, "40-49"),
                new StatisticsReport.ScoreRange(30, 40, "30-39")
        };

        // Calculate distribution of scores
        Map<String, Long> scoreDistribution = Arrays.stream(ranges)
                .collect(Collectors.toMap(
                        StatisticsReport.ScoreRange::getLabel,
                        range -> averageScores.stream()
                                .filter(score -> score >= range.getMin() && score < range.getMax())
                                .count()
                ));

        // Calculate statistical metrics
        double meanScore = averageScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double medianScore = calculateMedian(averageScores);

        double standardDeviation = Math.sqrt(
                averageScores.stream()
                        .mapToDouble(score -> Math.pow(score - meanScore, 2))
                        .average()
                        .orElse(0.0)
        );

        // Build and return the statistics report
        return new StatisticsReport(
                sessionClass.getAcademicSession().getSessionName().getName(),
                sessionClass.getClassBlock().getName(),
                scoreDistribution,
                meanScore,
                medianScore,
                standardDeviation,
                averageScores.size()
        );
    }

    @Transactional(readOnly = true)
    public TermStatisticsReport calculateTermStatistics(Long sessionId, Long classLevelId, Long termId) {
        // Fetch session, class block, and term to validate
        SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(sessionId,classLevelId)
                .orElseThrow(() -> new NotFoundException("Session class  not found"));
        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found"));

        // Fetch all positions for the given session, class, and term
        Collection<Position> positions = positionRepository.findAllBySessionClassAndAcademicYearAndStudentTerm(sessionClass, sessionClass.getAcademicSession(), studentTerm);

        if (positions.isEmpty()) {
            throw new NotFoundException("No average scores found for the specified term, session, and class");
        }

        // Extract average scores
        List<Double> averageScores = positions.stream()
                .map(Position::getAverageScore)
                .collect(Collectors.toList());

        // Define score ranges
        StatisticsReport.ScoreRange[] ranges = {
                new StatisticsReport.ScoreRange(90, 101, "90-100"), // 101 to include 100
                new StatisticsReport.ScoreRange(80, 90, "80-89"),
                new StatisticsReport.ScoreRange(70, 80, "70-79"),
                new StatisticsReport.ScoreRange(60, 70, "60-69"),
                new StatisticsReport.ScoreRange(50, 60, "50-59"),
                new StatisticsReport.ScoreRange(40, 50, "40-49"),
                new StatisticsReport.ScoreRange(30, 40, "30-39")
        };

        // Calculate distribution of scores
        Map<String, Long> scoreDistribution = Arrays.stream(ranges)
                .collect(Collectors.toMap(
                        StatisticsReport.ScoreRange::getLabel,
                        range -> averageScores.stream()
                                .filter(score -> score >= range.getMin() && score < range.getMax())
                                .count()
                ));

        // Calculate statistical metrics
        double meanScore = averageScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double medianScore = calculateMedian(averageScores);

        double standardDeviation = Math.sqrt(
                averageScores.stream()
                        .mapToDouble(score -> Math.pow(score - meanScore, 2))
                        .average()
                        .orElse(0.0)
        );

        // Build and return the statistics report
        return new TermStatisticsReport(
                sessionClass.getAcademicSession().getSessionName().getName(),
                sessionClass.getClassBlock().getName(),
                studentTerm.getName(),
                scoreDistribution,
                meanScore,
                medianScore,
                standardDeviation,
                averageScores.size()
        );
    }


    private double calculateMedian(List<Double> scores) {
        if (scores.isEmpty()) return 0.0;
        List<Double> sortedScores = scores.stream().sorted().toList();
        int size = sortedScores.size();
        if (size % 2 == 0) {
            return (sortedScores.get(size / 2 - 1) + sortedScores.get(size / 2)) / 2.0;
        } else {
            return sortedScores.get(size / 2);
        }
    }



    @Override
    public List<SessionAverage> getTop5StudentsPerClass(Long classBlockId, Long academicYearId) {
        SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(classBlockId,academicYearId)
                .orElseThrow(() -> new NotFoundException("Session class  not found"));
        return sessionAverageRepository.findAllBySessionClassAndAcademicYear(sessionClass, sessionClass.getAcademicSession());
    }

    @Override
    public Map<ClassBlock, List<SessionAverage>> getTop5StudentsForAllClasses(Long academicYearId) {

        List<SessionAverage> allStudents = sessionAverageRepository.findTop5ByAcademicYearId(academicYearId);

//        return allStudents.stream()
//                .collect(Collectors.groupingBy(SessionAverage::getSessionClass,
//                        Collectors.collectingAndThen(Collectors.toList(),
//                                list -> list.stream().limit(5).toList())));
        return  null;
    }


}
