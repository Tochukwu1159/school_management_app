package examination.teacherAndStudents.service.serviceImpl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.EmailDetails;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.PositionService;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.StudentTerm;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static examination.teacherAndStudents.utils.StudentTerm.FIRST;

@Service
public class PositionServiceImpl implements PositionService {
    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private ClassLevelRepository classLevelRepository;
    @Autowired
    private EmailService emailService;

    private static final String RESULT_FILE = "/Users/mac/Documents/ResultStatement.pdf";
    @Autowired
    private ClassBlockRepository classBlockRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AcademicSessionRepository academicSessionRepository;


    @Transactional
    public void updateAllPositionsForAClass(Long classLevelId,Long  sessionId,  StudentTerm term) {

        AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + classLevelId));


        // Get all users in the specified ClassLevel
        ClassBlock studentClass = classBlockRepository.findById(classLevelId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassLevel not found with ID: " + classLevelId));

        // Get all users in the specified ClassLevel
        ClassLevel classLevel = classLevelRepository.findById(studentClass.getClassLevel().getId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassLevel not found with ID: " + studentClass.getClassLevel().getId()));

        List<Profile> studentClassBlock = studentClass.getStudentList();

        if (studentClassBlock.isEmpty()) {
            throw new IllegalStateException("No users found in the specified ClassLevel.");
        }

        // Fetch all positions for the given class level and term in a single query
        List<Position> positions = positionRepository.findAllByClassBlockAndAcademicYearAndTerm(studentClass,academicSession, term);

        // Create a map for quick access to positions by user
        Map<Profile, Position> positionMap = positions.stream()
                .collect(Collectors.toMap(Position::getUserProfile, Function.identity()));

        // Sort users by average score in descending order
        List<Profile> sortedUsers = studentClassBlock.stream()
                .sorted(Comparator.comparingDouble(user -> positionMap.getOrDefault(user, new Position()).getAverageScore())
                        .reversed())
                .collect(Collectors.toList());

        // Update positions based on the sorted order
        int rank = 1;
        for (Profile user : sortedUsers) {
            Position position = positionMap.getOrDefault(user, new Position());
            position.setClassBlock(studentClass);
            position.setUserProfile(user);
            position.setTerm(term);
            position.setPositionRank(rank++);
            positionRepository.save(position);
        }
    }



    public String getPositionRank(int position) {
        return getOrdinalSuffix(position);
    }

    public static String getOrdinalSuffix(int position) {
        if (position >= 10 && position <= 20) {
            return position + "th";
        } else {
            switch (position % 10) {
                case 1:
                    return position + "st";
                case 2:
                    return position + "nd";
                case 3:
                    return position + "rd";
                default:
                    return position + "th";
            }
        }
    }

    public void generateResultSummaryPdf(Long studentId, Long classLevelId,Long sessionId, StudentTerm term) throws IOException, DocumentException {
        // Fetch user's scores, average, and position for the specific class
        try{
            AcademicSession session = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student academic session not found with ID: " + sessionId));

            ClassBlock userClass = classBlockRepository.findById(classLevelId)
                .orElseThrow(() -> new ResourceNotFoundException("student class not found with ID: " + classLevelId));

            ClassLevel generalClass = classLevelRepository.findById(classLevelId)
                    .orElseThrow(() -> new ResourceNotFoundException("ClassLevel not found with ID: " + classLevelId));

        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

            Profile userProfile = profileRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found with ID: " + user.getId()));


        List<Score> scores = scoreRepository.findAllByUserProfileAndClassBlockAndAcademicYearAndTerm(userProfile, userClass, session, term);
//            System.out.println(scores);
            System.out.println(scores.stream().collect(Collectors.toList()));
//            System.out.println(scores.stream().toArray());
        Position position = positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndTerm(userProfile, userClass,session, term);
        List<Result> results = resultRepository.findAllByUserProfileAndClassBlockAndAcademicYearAndTerm(userProfile, userClass,session,  term);
        if (results.isEmpty()) {
            throw new NotFoundException("No results found for the user with ID: " + user.getId());
        }

        // Create PDF document
        Document document = new Document();
        OutputStream outputStream = new FileOutputStream(RESULT_FILE);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);

        // Load School Logo
        Image schoolLogo = Image.getInstance("/Users/mac/Documents/school.png");
        schoolLogo.scaleAbsolute(60, 60); // Adjust the size as needed

        // Add School Logo and Name
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3});
        headerTable.setSpacingBefore(1);

        PdfPCell logoCell = new PdfPCell(schoolLogo, true);
        logoCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(logoCell);

        PdfPCell schoolNameCell = new PdfPCell(new Phrase("THE ACADEMY OF ROYALTIES", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK)));
        schoolNameCell.setBorder(Rectangle.NO_BORDER);
        schoolNameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        schoolNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(schoolNameCell);

        document.open();

        // Add School Logo and Name to the document
        document.add(headerTable);

        // Add custom font
        Font fontTitle = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
        Font fontSubtitle = new Font(Font.FontFamily.HELVETICA, 18, Font.NORMAL, BaseColor.DARK_GRAY);
        Font fontBody = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);

        // Add certificate title
        Paragraph title = new Paragraph("Student Report Card", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(5);
        title.setSpacingAfter(20);
        document.add(title);

        // Add user information
        Paragraph userInfo = new Paragraph(
                "This is to certify that " + user.getFirstName() + " " + user.getLastName() +
                        " has successfully completed the academic term with outstanding performance.",
                fontSubtitle
        );
        userInfo.setAlignment(Element.ALIGN_CENTER);
        userInfo.setSpacingAfter(10);
        document.add(userInfo);

        // Add user details table
        PdfPTable userDetailsTable = new PdfPTable(4);
        userDetailsTable.setWidthPercentage(100);
        userDetailsTable.setSpacingBefore(20);

        Font fontTableHeader = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font fontTableCell = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

        PdfPCell cellUserId = new PdfPCell(new Phrase("Student ID", fontTableHeader));
        cellUserId.setBackgroundColor(BaseColor.GRAY);
        PdfPCell cellUserName = new PdfPCell(new Phrase("Student Name", fontTableHeader));
        cellUserName.setBackgroundColor(BaseColor.GRAY);
        PdfPCell cellUserClass = new PdfPCell(new Phrase("Student Class", fontTableHeader));
        cellUserClass.setBackgroundColor(BaseColor.GRAY);
        PdfPCell cellUserClassTerm = new PdfPCell(new Phrase("Student Term", fontTableHeader));
        cellUserClassTerm.setBackgroundColor(BaseColor.GRAY);

        userDetailsTable.addCell(cellUserId);
        userDetailsTable.addCell(cellUserName);
        userDetailsTable.addCell(cellUserClass);
            userDetailsTable.addCell(cellUserClassTerm);

        PdfPCell cellUserIdValue = new PdfPCell(new Phrase(String.valueOf(user.getId()), fontTableCell));
        PdfPCell cellUserNameValue = new PdfPCell(new Phrase(user.getFirstName() + " " + user.getLastName(), fontTableCell));
        PdfPCell cellUserIdClass = new PdfPCell(new Phrase(String.valueOf(userProfile.getClassBlock().getCurrentStudentClassName()), fontTableCell));
            PdfPCell cellUserClassTermValue = new PdfPCell(new Phrase(String.valueOf(term), fontTableCell));


            userDetailsTable.addCell(cellUserIdValue);
        userDetailsTable.addCell(cellUserNameValue);
        userDetailsTable.addCell(cellUserIdClass);
            userDetailsTable.addCell(cellUserClassTermValue);

        document.add(userDetailsTable);

        // Add subjects and scores
        PdfPTable scoreTable = new PdfPTable(6); // Increased the number of columns to include totalMark and Grade
        scoreTable.setWidthPercentage(100);
        scoreTable.setSpacingBefore(20);

        addTableHeader(scoreTable, fontBody);

        for (Score score : scores) {
            Result result = results.stream()
                    .filter(r -> r.getSubjectName().equals(score.getSubjectName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Result not found for subject: " + score.getSubjectName()));

            addRowToTable(scoreTable, score.getSubjectName(),
                    Integer.toString(score.getAssessmentScore()),
                    Integer.toString(score.getExamScore()),
                    Double.toString(result.getTotalMarks()),
                    result.getGrade(),
                    result.getRating());
        }

        document.add(scoreTable);

        // Add teacher's remark
        Paragraph remark = new Paragraph(
                "Teacher's Remark: " + getTeachersRemark(position.getAverageScore()),
                fontSubtitle
        );
        remark.setAlignment(Element.ALIGN_LEFT);
        remark.setSpacingAfter(20);
        document.add(remark);

        // Add average and position
        Paragraph summary = new Paragraph(
                "Average Score: " + position.getAverageScore() + "  |  Position: " + getPositionRank(position.getPositionRank()),
                fontSubtitle
        );
        summary.setAlignment(Element.ALIGN_CENTER);
        summary.setSpacingAfter(20);
        document.add(summary);

        // Add signature line
        Paragraph signature = new Paragraph("___________________________\n");
        signature.setAlignment(Element.ALIGN_RIGHT);

        Image signatureImage = loadSignatureImage();
        signatureImage.setAbsolutePosition(400, 900);
        signature.add(new Chunk(new VerticalPositionMark(), 0, true));
        signature.add(new Chunk(signatureImage, 400, 0, true));

        document.add(signature);
        document.close();

        // Email result summary
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(user.getEmail())
                .subject("Result Summary")
                .templateName("email-template-result")
                .model(createModelWithData(user))
                .attachmentPath(RESULT_FILE)
                .build();
        emailService.sendEmailWithAttachment(emailDetails);

        } catch (DocumentException | IOException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }

    private Image loadSignatureImage() throws IOException, BadElementException {
        // Load the signature image
        Image signatureImage = Image.getInstance("/Users/mac/Documents/principal.png"); // Replace with the actual path
        signatureImage.scaleToFit(100, 50); // Adjust the width and height as needed
        return signatureImage;

    }

    private String getTeachersRemark(double averageScore) {
        // Add your logic for generating the teacher's remark based on the average score
        // You can customize this method according to your requirements
        if (averageScore >= 70) {
            return "Excellent performance! Keep up the good work.";
        } else if (averageScore >= 50) {
            return "Good effort. Continue to improve.";
        } else {
            return "Work harder to improve your performance.";
        }
    }


    private void addTableHeader(PdfPTable table, Font font) {
        Stream.of("Subject", "Assessment Score", "Exam Score", "Total Mark", "Grade", "Rating")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle, font));
                    table.addCell(header);
                });
    }

    private void addRowToTable(PdfPTable table, String... values) {
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value));
            cell.setBorderWidth(2);
            table.addCell(cell);
        }
    }




    private Map<String, Object> createModelWithData(User user) {
        Map<String, Object> model = new HashMap<>();

        // Add data to the model
        model.put("name", user.getFirstName() + " " + user.getLastName());
        model.put("greeting", "Dear " + user.getFirstName() + ",");
        model.put("message", "Attached is your result summary.");

        // You can add more data as needed for your email template

        return model;
    }



}
