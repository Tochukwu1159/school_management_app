package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.HomeworkRequest;
import examination.teacherAndStudents.dto.HomeworkResponse;
import examination.teacherAndStudents.dto.HomeworkSubmissionRequest;
import examination.teacherAndStudents.dto.HomeworkSubmissionResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.SubmissionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Validated
public class HomeworkService {

    private static final Logger logger = LoggerFactory.getLogger(HomeworkService.class);

    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    private final ProfileRepository profileRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final ClassBlockRepository classBlockRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public HomeworkResponse createHomework(@Valid HomeworkRequest request) {
        User teacher = verifyTeacherAccess();
        logger.info("Teacher {} creating homework for subject ID: {}", teacher.getEmail(), request.subjectId());

        Profile teacherProfile = profileRepository.findByUserId(teacher.getId())
                .orElseThrow(() -> new CustomNotFoundException("Teacher profile not found for ID: " + teacher.getId()));
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new CustomNotFoundException("Subject not found with ID: " + request.subjectId()));
        AcademicSession session = academicSessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found with ID: " + request.sessionId()));
        ClassBlock classBlock = classBlockRepository.findById(request.classId())
                .orElseThrow(() -> new CustomNotFoundException("Class block not found with ID: " + request.classId()));
        StudentTerm term = studentTermRepository.findById(request.termId())
                .orElseThrow(() -> new CustomNotFoundException("Student term not found with ID: " + request.termId()));

        Homework homework = Homework.builder()
                .subject(subject)
                .academicSession(session)
                .term(term)
                .classBlock(classBlock)
                .teacher(teacherProfile)
                .title(request.title())
                .mark(request.mark())
                .school(teacher.getSchool())
                .description(request.description())
                .fileUrl(request.fileUrl())
                .submissionDate(request.submissionDate())
                .createdAt(LocalDateTime.now())
                .build();

        Homework savedHomework = homeworkRepository.save(homework);
        return modelMapper.map(savedHomework, HomeworkResponse.class);
    }

    @Transactional
    public HomeworkSubmissionResponse submitHomework(@Valid HomeworkSubmissionRequest request) {
        User student = verifyStudentAccess();
        logger.info("Student {} submitting homework for ID: {}", student.getEmail(), request.homeworkId());

        Profile studentProfile = profileRepository.findByUserId(student.getId())
                .orElseThrow(() -> new CustomNotFoundException("Student profile not found for ID: " + student.getId()));
        Homework homework = homeworkRepository.findById(request.homeworkId())
                .orElseThrow(() -> new CustomNotFoundException("Homework not found with ID: " + request.homeworkId()));

        if (!studentProfile.getSessionClass().getClassBlock().getId().equals(homework.getClassBlock().getId())) {
            logger.warn("Student {} attempted to submit homework for another class block", student.getEmail());
            throw new AuthenticationFailedException("Student cannot submit homework for another class block");
        }

        SubmissionStatus status = request.submittedAt().isAfter(homework.getSubmissionDate())
                ? SubmissionStatus.LATE
                : SubmissionStatus.SUBMITTED;

        HomeworkSubmission submission = HomeworkSubmission.builder()
                .homework(homework)
                .student(studentProfile)
                .fileUrl(request.fileUrl())
                .submittedAt(request.submittedAt())
                .status(status)
                .build();

        HomeworkSubmission savedSubmission = submissionRepository.save(submission);
        return modelMapper.map(savedSubmission, HomeworkSubmissionResponse.class);
    }

    @Transactional
    public HomeworkSubmissionResponse gradeSubmission(Long submissionId, Double obtainedMark) {
        User teacher = verifyTeacherAccess();
        logger.info("Teacher {} grading submission ID: {}", teacher.getEmail(), submissionId);

        HomeworkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new CustomNotFoundException("Submission not found with ID: " + submissionId));

        if (!submission.getHomework().getTeacher().getUser().getId().equals(teacher.getId())) {
            logger.warn("Teacher {} attempted to grade submission not assigned to them", teacher.getEmail());
            throw new AuthenticationFailedException("Teacher can only grade their own homework submissions");
        }

        submission.setObtainedMark(obtainedMark);
        submission.setStatus(SubmissionStatus.GRADED);
        HomeworkSubmission updatedSubmission = submissionRepository.save(submission);
        return modelMapper.map(updatedSubmission, HomeworkSubmissionResponse.class);
    }

    @Transactional(readOnly = true)
    public Page<HomeworkResponse> getHomeworkBySubject(
            Long subjectId,
            Long classBlockId,
            Long termId,
            LocalDateTime submissionDate,
            String title,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        User user = verifyUserAccess();
        logger.info("User {} fetching homework for subject ID: {}", user.getEmail(), subjectId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Homework> homeworkPage;
        if (user.getRoles().contains(Roles.STUDENT)) {
            Profile studentProfile = profileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Student profile not found for ID: " + user.getId()));
            homeworkPage = homeworkRepository.findBySubjectIdAndFiltersForStudent(
                    subjectId,
                    classBlockId != null ? classBlockId : studentProfile.getSessionClass().getClassBlock().getId(),
                    termId,
                    submissionDate,
                    title,
                    user.getSchool().getId(),
                    pageable
            );
        } else {
            homeworkPage = homeworkRepository.findBySubjectIdAndFilters(
                    subjectId,
                    classBlockId,
                    termId,
                    submissionDate,
                    title,
                    user.getSchool().getId(),
                    pageable
            );
        }

        return homeworkPage.map(homework -> modelMapper.map(homework, HomeworkResponse.class));
    }
   @Transactional(readOnly = true)
    public Page<HomeworkSubmissionResponse> getSubmissionsByHomework(
            Long homeworkId,
            Long studentId,
            LocalDateTime submittedAt,
            SubmissionStatus status,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        User user = verifyTeacherOrAdminAccess();
        logger.info("User {} fetching submissions for homework ID: {}", user.getEmail(), homeworkId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<HomeworkSubmission> submissions = submissionRepository.findByHomeworkIdAndFilters(
                homeworkId,
                studentId,
                submittedAt,
                status,
                user.getSchool().getId(),
                pageable
        );

        return submissions.map(submission -> modelMapper.map(submission, HomeworkSubmissionResponse.class));
    }

    private User verifyUserAccess() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found with email: " + email));
    }

    private User verifyTeacherAccess() {
        User user = verifyUserAccess();
        if (!user.getRoles().contains(Roles.TEACHER)) {
            logger.warn("Unauthorized access attempt by user: {}", user.getEmail());
            throw new AuthenticationFailedException("Access restricted to TEACHER role");
        }
        return user;
    }

    private User verifyStudentAccess() {
        User user = verifyUserAccess();
        if (!user.getRoles().contains(Roles.STUDENT)) {
            logger.warn("Unauthorized access attempt by user: {}", user.getEmail());
            throw new AuthenticationFailedException("Access restricted to STUDENT role");
        }
        return user;
    }

    private User verifyTeacherOrAdminAccess() {
        User user = verifyUserAccess();
        if (!user.getRoles().contains(Roles.TEACHER) && !user.getRoles().contains(Roles.ADMIN)) {
            logger.warn("Unauthorized access attempt by user: {}", user.getEmail());
            throw new AuthenticationFailedException("Access restricted to TEACHER or ADMIN roles");
        }
        return user;
    }
}