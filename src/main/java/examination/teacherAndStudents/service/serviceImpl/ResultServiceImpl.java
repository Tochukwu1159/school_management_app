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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
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
    private final GradeRatingService gradeRatingService;


    @Autowired
    public ResultServiceImpl(ScoreService scoreService, UserRepository userRepository, ScoreRepository scoreRepository,
                             ResultRepository resultRepository,
                             PositionRepository positionRepository,
                             ClassLevelRepository classLevelRepository, ClassBlockRepository classBlockRepository, ProfileRepository profileRepository, AcademicSessionRepository academicSessionRepository, StudentTermRepository studentTermRepository, SessionAverageRepository sessionAverageRepository,  GradeRatingService gradeRatingService) {
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
            Result existingResult = resultRepository.findByUserProfileAndClassBlockIdAndSubjectNameAndAcademicYearAndStudentTerm(userProfile, studentClass.getId() ,subjectName,academicSession, studentTerm);

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
            result.setClassBlock(studentClass);
            result.setAcademicYear(academicSession);
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

        // Pre-fetch existing positions
        Map<Profile, Position> existingPositions = positionRepository.findByClassBlockAndAcademicYearAndStudentTerm(
                        classBlock, academicYear, studentTerm).stream()
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
            position.setClassBlock(classBlock);
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
                .orElseThrow(() -> new NotFoundException("Session not found"));

        // Fetch student term
        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found"));

        // Fetch all class blocks for the academic session
        List<ClassBlock> classBlocks = classBlockRepository.findByClassLevelAcademicYear(academicYear);

        if (classBlocks.isEmpty()) {
            throw new NotFoundException("No class blocks found for the specified session");
        }

        // Process each class block
        for (ClassBlock classBlock : classBlocks) {
            // Fetch all results for the specified session, class block, and term
            List<Result> results = resultRepository.findAllByClassBlockAndAcademicYearAndStudentTerm(
                    classBlock, academicYear, studentTerm);

            if (results.isEmpty()) {
                continue; // Skip if no results for this class block
            }

            // Group results by student profile
            Map<Profile, List<Result>> resultsByStudent = results.stream()
                    .collect(Collectors.groupingBy(Result::getUserProfile));

            // Pre-fetch existing positions for this class block
            Map<Profile, Position> existingPositions = positionRepository.findByClassBlockAndAcademicYearAndStudentTerm(
                            classBlock, academicYear, studentTerm).stream()
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
                position.setClassBlock(classBlock);
                position.setAcademicYear(academicYear);
                position.setStudentTerm(studentTerm);
                position.setAverageScore(averageScore);

                positionsToSave.add(position);
            });

            // Batch save all positions for this class block
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
            AcademicSession currentSession = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new NotFoundException("Current session not found with ID: " + sessionId));
            ClassBlock presentClass = classBlockRepository.findById(presentClassId)
                    .orElseThrow(() -> new NotFoundException("Present class not found with ID: " + presentClassId));
            AcademicSession futureSession = academicSessionRepository.findById(futureSessionId)
                    .orElseThrow(() -> new NotFoundException("Future session not found with ID: " + futureSessionId));
            ClassBlock futurePClass = classBlockRepository.findById(futurePClassId)
                    .orElseThrow(() -> new NotFoundException("Promoted class not found with ID: " + futurePClassId));
            ClassBlock futureFClass = classBlockRepository.findById(futureFClassId)
                    .orElseThrow(() -> new NotFoundException("Demoted class not found with ID: " + futureFClassId));

            // Fetch session averages for students in the current session and class
            List<SessionAverage> sessionAverages = sessionAverageRepository.findAllByClassBlockAndAcademicYear(presentClass, currentSession);

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
                    studentProfile.setClassBlock(futurePClass);
                    profileRepository.save(studentProfile);
                    promotedCount++;
                } else {
                    // Demote student
                    studentProfile.setClassBlock(futureFClass);
                    profileRepository.save(studentProfile);
                    demotedCount++;
                }
            }

            // Update class student counts in bulk
            futurePClass.setNumberOfStudents(futurePClass.getNumberOfStudents() + promotedCount);
            futureFClass.setNumberOfStudents(futureFClass.getNumberOfStudents() + demotedCount);
            classBlockRepository.save(futurePClass);
            classBlockRepository.save(futureFClass);

        } catch (NotFoundException e) {
            // Re-throw custom exceptions for clarity
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            throw new RuntimeException("An error occurred during student promotion: " + e.getMessage(), e);
        }
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

    @Override
    public void updateSessionAverageForJob(AcademicSession academicYear) {
        // Get all unique class blocks in the academic session
        List<ClassBlock> classBlocks = classBlockRepository.findByClassLevelAcademicYear(academicYear);

        for (ClassBlock classBlock : classBlocks) {
            // Get all student profiles in the class block
            List<Profile> studentProfiles = profileRepository.findByClassBlockAndClassBlock_ClassLevel_AcademicYear(classBlock, academicYear);

            for (Profile studentProfile : studentProfiles) {
                // Fetch the positions for all three terms
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

                    // Check if the SessionAverage already exists
                    SessionAverage sessionAverage = sessionAverageRepository.findByUserProfileAndAcademicYearAndClassBlock(
                            studentProfile, academicYear, classBlock
                    );

                    if (sessionAverage == null) {
                        // Create a new SessionAverage record
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
                        // Update existing SessionAverage record
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



    @Transactional(readOnly = true)
    public StatisticsReport calculateSessionStatistics(Long sessionId, Long classLevelId) {
        // Fetch session and class block to validate
        AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Academic session not found"));
        ClassBlock classBlock = classBlockRepository.findById(classLevelId)
                .orElseThrow(() -> new NotFoundException("Class level not found"));

        // Fetch all session averages for the given session and class
        List<SessionAverage> sessionAverages = sessionAverageRepository.findAllByAcademicYearAndClassBlock(academicSession, classBlock);

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
                academicSession.getSessionName().getName(),
                classBlock.getName(),
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
        AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Academic session not found"));
        ClassBlock classBlock = classBlockRepository.findById(classLevelId)
                .orElseThrow(() -> new NotFoundException("Class level not found"));
        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found"));

        // Fetch all positions for the given session, class, and term
        Collection<Position> positions = positionRepository.findByClassBlockAndAcademicYearAndStudentTerm(classBlock, academicSession, studentTerm);

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
                academicSession.getSessionName().getName(),
                classBlock.getName(),
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
    public List<SessionAverage> getTop5StudentsPerClass(ClassBlock classBlock, AcademicSession academicYear) {
        return sessionAverageRepository.findTop5ByClassBlockAndAcademicYearOrderByAverageScoreDesc(classBlock, academicYear);
    }

    @Override
    public Map<ClassBlock, List<SessionAverage>> getTop5StudentsForAllClasses(AcademicSession academicYear) {
        List<SessionAverage> allStudents = sessionAverageRepository.findTop5ByAcademicYear(academicYear);

        return allStudents.stream()
                .collect(Collectors.groupingBy(SessionAverage::getClassBlock,
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().limit(5).toList())));
    }


}
