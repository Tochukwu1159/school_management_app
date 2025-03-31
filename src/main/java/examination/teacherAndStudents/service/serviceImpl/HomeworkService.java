package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.HomeworkResponse;
import examination.teacherAndStudents.dto.HomeworkSubmissionResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.utils.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    private final ProfileRepository profileRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final ClassBlockRepository classBlockRepository;

    // Teacher uploads a homework assignment
    public Homework createHomework(Long teacherId, Long subjectId, Long sessionId,Long classId, Long termId, String title, String description, String fileUrl, LocalDateTime submissionDate) {
        Profile teacher = profileRepository.findById(teacherId)
                .orElseThrow(() -> new CustomNotFoundException("Teacher not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new CustomNotFoundException("Subject not found"));

        AcademicSession session = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found"));
        ClassBlock classBlock = classBlockRepository.findById(classId)
                .orElseThrow(() -> new CustomNotFoundException("Class  not found"));


        StudentTerm studentTerm = studentTermRepository.findById(termId)
                .orElseThrow(() -> new CustomNotFoundException("Student term not found"));

        Homework homework = Homework.builder()
                .subject(subject)
                .academicSession(session)
                .term(studentTerm)
                .classBlock(classBlock)
                .teacher(teacher)
                .title(title)
                .school(teacher.getUser().getSchool())
                .description(description)
                .fileUrl(fileUrl)
                .submissionDate(submissionDate)
                .createdAt(LocalDateTime.now())
                .build();
        return homeworkRepository.save(homework);
    }

    // Student submits homework
    public HomeworkSubmission submitHomework(Long homeworkId, String fileUrl) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile student = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Student not found"));

        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new CustomNotFoundException("Homework not found"));

        if(!student.getClassBlock().getId().equals(homework.getClassBlock().getId())) {
            throw new CustomNotFoundException("Student cannot access an assignment from another Class block");
        }

        SubmissionStatus status = LocalDateTime.now().isAfter(homework.getSubmissionDate())
                ? SubmissionStatus.LATE
                : SubmissionStatus.SUBMITTED;

        HomeworkSubmission submission = HomeworkSubmission.builder()

                .homework(homework)
                .student(student)
                .fileUrl(fileUrl)
                .submittedAt(LocalDateTime.now())
                .status(status)
                .build();

        return submissionRepository.save(submission);
    }


    // Teacher evaluates a submission
    public HomeworkSubmission gradeSubmission(Long submissionId, Double obtainedMark) {
        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new CustomNotFoundException("Submission not found"));

        submission.setObtainedMark(obtainedMark);
        submission.setStatus(SubmissionStatus.GRADED);
        return submissionRepository.save(submission);
    }

    // Get all homework by subject
    public Page<HomeworkResponse> getHomeworkBySubject(
            Long subjectId,
            Long classBlockId,
            Long termId,
            LocalDateTime submissionDate,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User profile not found"));

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Homework> homeworkPage = homeworkRepository.findBySubjectIdAndFilters(
                subjectId,
                classBlockId,
                termId,
                submissionDate,
                profile.getUser().getId(),
                pageable);

        return homeworkPage.map(this::mapToHomeworkResponse);
    }

    private HomeworkResponse mapToHomeworkResponse(Homework homework) {
        return HomeworkResponse.builder()
                .id(homework.getId())
                .subjectId(homework.getSubject().getId())
                .subjectName(homework.getSubject().getName())
                .academicSessionId(homework.getAcademicSession().getId())
                .academicSessionName(homework.getAcademicSession().getName())
                .classBlockId(homework.getClassBlock().getId())
                .classBlockName(homework.getClassBlock().getCurrentStudentClassName())
                .termId(homework.getTerm().getId())
                .termName(homework.getTerm().getName())
                .teacherId(homework.getTeacher().getId())
                .teacherName(homework.getTeacher().getUser().getFirstName() + " " + homework.getTeacher().getUser().getLastName())
                .title(homework.getTitle())
                .description(homework.getDescription())
                .fileUrl(homework.getFileUrl())
                .createdAt(homework.getCreatedAt())
                .submissionDate(homework.getSubmissionDate())
                .build();
    }

    // Get all submissions for a homework
    public Page<HomeworkSubmissionResponse> getSubmissionsByHomework(
            Long homeworkId,
            Long studentId,
            LocalDateTime submittedAt,
            SubmissionStatus status,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User profile not found"));

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<HomeworkSubmission> submissions = submissionRepository.findByHomeworkIdAndFilters(
                homeworkId,
                studentId,
                submittedAt,
                status,
                profile.getUser().getId(),
                pageable);

        return submissions.map(this::mapToSubmissionResponse);
    }

    private HomeworkSubmissionResponse mapToSubmissionResponse(HomeworkSubmission submission) {
        return HomeworkSubmissionResponse.builder()
                .id(submission.getId())
                .homeworkId(submission.getHomework().getId())
                .homeworkTitle(submission.getHomework().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getUser().getFirstName() + " " + submission.getStudent().getUser().getLastName())
                .fileUrl(submission.getFileUrl())
                .submittedAt(submission.getSubmittedAt())
                .obtainedMark(submission.getObtainedMark())
                .status(submission.getStatus())
                .build();
    }
}
