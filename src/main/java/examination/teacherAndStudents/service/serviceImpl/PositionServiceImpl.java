package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.ResultSummary;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.templateService.ReportCardService;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.PositionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
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
    private SchoolRepository schoolRepository;
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

        // Fetch all students in the class block
        List<Profile> students = classBlock.getStudentList();
        if (students.isEmpty()) {
            throw new IllegalStateException("No students found in the specified class block.");
        }

        // Fetch positions for the class block, academic session, and student term
        List<Position> existingPositions = positionRepository.findAllByClassBlockAndAcademicYearAndStudentTerm(
                classBlock, academicSession, studentTerm
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
            position.setClassBlock(classBlock);
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

            List<Score> scores = scoreRepository.findAllByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                    userProfile, userClass, session, studentTerm);

            List<Result> results = resultRepository.findAllByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                   userProfile, userClass, session, studentTerm);

            Position position = positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                    userProfile, userClass, session, studentTerm);

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




//    public String getPositionRank(int rank) {
//        return getOrdinalSuffix(rank);
//    }
//
//    private static String getOrdinalSuffix(int rank) {
//        if (rank >= 11 && rank <= 13) {
//            return rank + "th";
//        }
//        return switch (rank % 10) {
//            case 1 -> rank + "st";
//            case 2 -> rank + "nd";
//            case 3 -> rank + "rd";
//            default -> rank + "th";
//        };
//    }

//    public void generateResultSummaryPdf(Long studentId, Long classLevelId, Long sessionId, Long term) {
//        try {
//            // Fetch required data
//            AcademicSession session = academicSessionRepository.findById(sessionId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + sessionId));
//
//            StudentTerm studentTerm = studentTermRepository.findById(term)
//                    .orElseThrow(() -> new ResourceNotFoundException("Student term not found with ID: " + term));
//
//            ClassBlock userClass = classBlockRepository.findById(classLevelId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Student class not found with ID: " + classLevelId));
//
//            User user = userRepository.findById(studentId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
//
//            Profile userProfile = profileRepository.findByUser(user)
//                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found with ID: " + user.getId()));
//
//            List<Score> scores = scoreRepository.findAllByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
//                    userProfile, userClass, session, studentTerm);
//
//            Position position = positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
//                    userProfile, userClass, session, studentTerm);
//
//            List<Result> results = resultRepository.findAllByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
//                    userProfile, userClass, session, studentTerm);
//
//            if (results.isEmpty()) {
//                throw new NotFoundException("No results found for the user with ID: " + user.getId());
//            }
//
//            // Create PDF document
//            try (OutputStream outputStream = new FileOutputStream(RESULT_FILE)) {
//                Document document = new Document();
//                PdfWriter.getInstance(document, outputStream);
//                document.open();
//
//                // Add header (school logo and name)
//                addDocumentHeader(document);
//
//                // Add title
//                addDocumentTitle(document, "Student Report Card");
//
//                // Add user information
//                addUserInformation(document, user);
//
//                // Add user details
//                addUserDetailsTable(document, user, userProfile, term);
//
//                // Add scores table
//                addScoresTable(document, scores, results);
//
//                // Add teacher's remark
//                addTeacherRemark(document, position);
//
//                // Add average and position
//                addSummary(document, position);
//
//                // Add signature
//                addSignature(document);
//
//                document.close();
//            }
//
//            // Send email with the result summary attached
//            sendResultSummaryEmail(user);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error generating result summary PDF", e);
//        }
//    }
//
//    private void addDocumentHeader(Document document) throws DocumentException, IOException {
//        Image schoolLogo = Image.getInstance("/Users/user/Documents/school.png");
//        schoolLogo.scaleAbsolute(60, 60);
//
//        PdfPTable headerTable = new PdfPTable(2);
//        headerTable.setWidthPercentage(100);
//        headerTable.setWidths(new int[]{1, 3});
//        headerTable.setSpacingBefore(1);
//
//        PdfPCell logoCell = new PdfPCell(schoolLogo, true);
//        logoCell.setBorder(Rectangle.NO_BORDER);
//        headerTable.addCell(logoCell);
//
//        PdfPCell schoolNameCell = new PdfPCell(new Phrase("THE ACADEMY OF ROYALTIES",
//                new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
//        schoolNameCell.setBorder(Rectangle.NO_BORDER);
//        schoolNameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
//        schoolNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//        headerTable.addCell(schoolNameCell);
//
//        document.add(headerTable);
//    }
//
//    private void addDocumentTitle(Document document, String titleText) throws DocumentException {
//        Paragraph title = new Paragraph(titleText, new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY));
//        title.setAlignment(Element.ALIGN_CENTER);
//        title.setSpacingBefore(5);
//        title.setSpacingAfter(20);
//        document.add(title);
//    }
//
//    private void addUserInformation(Document document, User user) throws DocumentException {
//        Paragraph userInfo = new Paragraph(
//                "This is to certify that " + user.getFirstName() + " " + user.getLastName() +
//                        " has successfully completed the academic term with outstanding performance.",
//                new Font(Font.FontFamily.HELVETICA, 18, Font.NORMAL, BaseColor.DARK_GRAY)
//        );
//        userInfo.setAlignment(Element.ALIGN_CENTER);
//        userInfo.setSpacingAfter(10);
//        document.add(userInfo);
//    }
//
//    private void addUserDetailsTable(Document document, User user, Profile userProfile, Long term) throws DocumentException {
//        PdfPTable userDetailsTable = new PdfPTable(4);
//        userDetailsTable.setWidthPercentage(100);
//        userDetailsTable.setSpacingBefore(20);
//
//        addTableHeaders(userDetailsTable, "Student ID", "Student Name", "Student Class", "Student Term");
//
//        addTableRow(userDetailsTable,
//                String.valueOf(user.getId()),
//                user.getFirstName() + " " + user.getLastName(),
//                userProfile.getClassBlock().getCurrentStudentClassName(),
//                term.toString() + " Term");
//
//        document.add(userDetailsTable);
//    }

//    private void addScoresTable(Document document, List<Score> scores, List<Result> results) throws DocumentException {
//        PdfPTable scoreTable = new PdfPTable(6);
//        scoreTable.setWidthPercentage(100);
//        scoreTable.setSpacingBefore(20);
//
//        addTableHeaders(scoreTable, "Subject", "Assessment Score", "Exam Score", "Total Mark", "Grade", "Rating");
//
//        for (Score score : scores) {
//            Result result = results.stream()
//                    .filter(r -> r.getSubjectName().equals(score.getSubjectName()))
//                    .findFirst()
//                    .orElseThrow(() -> new RuntimeException("Result not found for subject: " + score.getSubjectName()));
//
//            addTableRow(scoreTable,
//                    score.getSubjectName(),
//                    String.valueOf(score.getAssessmentScore()),
//                    String.valueOf(score.getExamScore()),
//                    String.valueOf(result.getTotalMarks()),
//                    result.getGrade(),
//                    result.getRating());
//        }
//
//        document.add(scoreTable);
//    }
//
//    private void addTeacherRemark(Document document, Position position) throws DocumentException {
//        Paragraph remark = new Paragraph(
//                "Teacher's Remark: " + generateTeacherRemark(position.getAverageScore()),
//                new Font(Font.FontFamily.HELVETICA, 18, Font.NORMAL, BaseColor.DARK_GRAY)
//        );
//        remark.setAlignment(Element.ALIGN_LEFT);
//        remark.setSpacingAfter(20);
//        document.add(remark);
//    }
//
//    private void addSummary(Document document, Position position) throws DocumentException {
//        Paragraph summary = new Paragraph(
//                "Average Score: " + position.getAverageScore() + "  |  Position: " + getPositionRank(position.getPositionRank()),
//                new Font(Font.FontFamily.HELVETICA, 18, Font.NORMAL, BaseColor.DARK_GRAY)
//        );
//        summary.setAlignment(Element.ALIGN_CENTER);
//        summary.setSpacingAfter(20);
//        document.add(summary);
//    }
//
//    private void addSignature(Document document) throws DocumentException, IOException {
//        Image signatureImage = Image.getInstance("/Users/user/Documents/principal.png");
//        signatureImage.scaleToFit(100, 50);
//        signatureImage.setAbsolutePosition(400, 100);
//        document.add(signatureImage);
//    }
//
//    private void sendResultSummaryEmail(User user) {
//        EmailDetails emailDetails = EmailDetails.builder()
//                .recipient(user.getEmail())
//                .subject("Result Summary")
//                .templateName("email-template-result")
//                .model(createEmailModel(user))
//                .attachmentPath(RESULT_FILE)
//                .build();
//        emailService.sendEmailWithAttachment(emailDetails);
//    }
//
//    private String generateTeacherRemark(double averageScore) {
//        if (averageScore >= 70) return "Excellent performance! Keep up the good work.";
//        if (averageScore >= 50) return "Good effort. Continue to improve.";
//        return "Work harder to improve your performance.";
//    }
//
//    private Map<String, Object> createEmailModel(User user) {
//        Map<String, Object> model = new HashMap<>();
//        model.put("name", user.getFirstName() + " " + user.getLastName());
//        model.put("greeting", "Dear " + user.getFirstName() + ",");
//        model.put("message", "Attached is your result summary.");
//        return model;
//    }
//
//    private void addTableHeaders(PdfPTable table, String... headers) {
//        for (String header : headers) {
//            PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
//            cell.setBackgroundColor(BaseColor.GRAY);
//            cell.setBorderWidth(2);
//            table.addCell(cell);
//        }
//    }
//
//    private void addTableRow(PdfPTable table, String... values) {
//        for (String value : values) {
//            PdfPCell cell = new PdfPCell(new Phrase(value));
//            cell.setBorderWidth(1);
//            table.addCell(cell);
//        }
//    }



}
