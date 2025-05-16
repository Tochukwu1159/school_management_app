package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.EntityAlreadyExistException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.error_handler.UnauthorizedException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ClassSubjectService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassSubjectServiceImpl implements ClassSubjectService {

    private final ClassSubjectRepository classSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final ClassBlockRepository classBlockRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ClassLevelRepository classLevelRepository;

    @Override
    @Transactional
    public List<ClassSubjectResponse> saveClassSubject( ClassSubjectRequest request) {
        User validUser = validateAdminUser();
        School userSchool = validUser.getSchool();

        // Validate ClassBlock
        ClassBlock classBlock = classBlockRepository.findById(request.getClassBlockId())
                .orElseThrow(() -> new NotFoundException("ClassBlock with id " + request.getClassBlockId() + " not found"));
        if (!classBlock.getSchool().getId().equals(userSchool.getId())) {
            throw new IllegalArgumentException("ClassBlock does not belong to the user's school");
        }

        // Validate AcademicSession
        AcademicSession academicSession = academicSessionRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new NotFoundException("AcademicSession with id " + request.getAcademicYearId() + " not found"));
        if (!academicSession.getSchool().getId().equals(userSchool.getId())) {
            throw new IllegalArgumentException("AcademicSession does not belong to the user's school");
        }

        List<ClassSubject> classSubjectsToSave = new ArrayList<>();
        List<ClassSubjectResponse> responses = new ArrayList<>();

        // Process each subjectId
        for (Long subjectId : request.getSubjectIds()) {
            // Validate Subject
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new NotFoundException("Subject with id " + subjectId + " not found"));

            // Check for existing ClassSubject
            boolean exists = classSubjectRepository.existsBySubjectAndClassBlockAndAcademicYear(
                    subject, classBlock, academicSession
            );
            if (exists) {
                continue; // Skip if already exists to avoid duplicate entries
            }

            // Create ClassSubject
            ClassSubject classSubject = ClassSubject.builder()
                    .subject(subject)
                    .classBlock(classBlock)
                    .school(userSchool)
                    .academicYear(academicSession)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            classSubjectsToSave.add(classSubject);
        }

        // Batch save all new ClassSubjects
        if (!classSubjectsToSave.isEmpty()) {
            List<ClassSubject> savedClassSubjects = classSubjectRepository.saveAll(classSubjectsToSave);
            responses = savedClassSubjects.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return responses;
    }
    @Override
    @Transactional(readOnly = true)
    public ClassSubjectResponse getClassSubjectById(Long id) {
        validateAdminUser();
        ClassSubject classSubject = classSubjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ClassSubject with id " + id + " not found"));
        return toResponse(classSubject);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassSubjectResponse> getAllClassSubjects(
            Long academicYearId, Long subjectId, Long classSubjectId, String subjectName, Long subClassId,
            int page, int size, String sortBy, String sortDirection) {
        validateAdminUser();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ClassSubject> classSubjects = classSubjectRepository.findAllWithFilters(
                academicYearId, subjectId, classSubjectId, subjectName, subClassId, pageable);

        return classSubjects.map(this::toResponse);
    }

    @Override
    @Transactional
    public void deleteClassSubject(Long id) {
        validateAdminUser();
        ClassSubject classSubject = classSubjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ClassSubject with id " + id + " not found"));
        classSubjectRepository.delete(classSubject);
        log.info("ClassSubject deleted [id={}]", id);
    }

    @Override
    @Transactional
    public void assignClassSubjectToTeacher(TeacherAssignmentRequest request) {
        validateAdminUser();

        ClassLevel classLevel = classLevelRepository.findByIdAndAcademicYearId(request.getClassLevelId(), request.getSessionId())
                .orElseThrow(() -> new CustomNotFoundException("Class Level  not found"));

        ClassBlock classBlock = classBlockRepository.findById(request.getClassBlockId())
                .orElseThrow(() -> new CustomNotFoundException("Student Class not found"));

        if (request.getAssignments() == null || request.getAssignments().isEmpty()) {
            throw new IllegalArgumentException("Assignments list cannot be empty");
        }

        for (TeacherAssignmentRequest.Assignment assignment : request.getAssignments()) {
            // Validate ClassSubject
            ClassSubject classSubject = classSubjectRepository.findById(assignment.getClassSubjectId())
                    .orElseThrow(() -> new NotFoundException("ClassSubject with id " + assignment.getClassSubjectId() + " not found"));

            // Validate Teacher
            User teacher = userRepository.findById(assignment.getTeacherId())
                    .orElseThrow(() -> new NotFoundException("Teacher with id " + assignment.getTeacherId() + " not found"));

            Profile teacherProfile = profileRepository.findById(assignment.getTeacherId())
                    .orElseThrow(() -> new NotFoundException("Teacher with id " + assignment.getTeacherId() + " not found"));

            if (!teacher.getRoles().contains(Roles.TEACHER)) {
                throw new IllegalArgumentException("User with id " + assignment.getTeacherId() + " is not a teacher");
            }

            // Check if the ClassSubject already has a teacher assigned
            if (classSubject.getTeacher() != null) {
                throw new EntityAlreadyExistException("ClassSubject with id " + assignment.getClassSubjectId() + " already has a teacher assigned");
            }

            // Assign teacher
            classSubject.setTeacher(teacherProfile);
            classSubject.setUpdatedAt(LocalDateTime.now());
            classSubjectRepository.save(classSubject);
            log.info("Assigned teacher [teacherId={}] to ClassSubject [classSubjectId={}]", teacher.getId(), classSubject.getId());
        }
    }

    @Override
    @Transactional
    public void updateClassSubjectTeacherAssignment(TeacherAssignmentRequest request) {
        validateAdminUser();

        ClassLevel classLevel = classLevelRepository.findByIdAndAcademicYearId(request.getClassLevelId(), request.getSessionId())
                .orElseThrow(() -> new CustomNotFoundException("Class Level  not found"));

        ClassBlock classBlock = classBlockRepository.findById(request.getClassBlockId())
                .orElseThrow(() -> new CustomNotFoundException("Student Class not found"));

        if (request.getAssignments() == null || request.getAssignments().isEmpty()) {
            throw new IllegalArgumentException("Assignments list cannot be empty");
        }

        for (TeacherAssignmentRequest.Assignment assignment : request.getAssignments()) {
            // Validate ClassSubject
            ClassSubject classSubject = classSubjectRepository.findById(assignment.getClassSubjectId())
                    .orElseThrow(() -> new NotFoundException("ClassSubject with id " + assignment.getClassSubjectId() + " not found"));

            // Validate Teacher
            User teacher = userRepository.findById(assignment.getTeacherId())
                    .orElseThrow(() -> new NotFoundException("Teacher with id " + assignment.getTeacherId() + " not found"));

            Profile teacherProfile = profileRepository.findByUser(teacher)
                    .orElseThrow(() -> new NotFoundException("Teacher with id " + assignment.getTeacherId() + " not found"));

            if (!teacher.getRoles().contains(Roles.TEACHER)) {
                throw new IllegalArgumentException("User with id " + assignment.getTeacherId() + " is not a teacher");
            }

            // Update teacher assignment (even if no teacher was previously assigned)
            classSubject.setTeacher(teacherProfile);
            classSubject.setUpdatedAt(LocalDateTime.now());
            classSubjectRepository.save(classSubject);
            log.info("Updated teacher assignment [teacherId={}] for ClassSubject [classSubjectId={}]", teacher.getId(), classSubject.getId());
        }
    }

    private User validateAdminUser() {
        String email = getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        if (!user.getRoles().contains(Roles.ADMIN)) {
            throw new UnauthorizedException("Please login as an Admin");
        }

        return user;
    }

    private String getAuthenticatedUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            log.error("Failed to retrieve authenticated user email", e);
            throw new UnauthorizedException("Unable to authenticate user");
        }
    }

    private ClassSubjectResponse toResponse(ClassSubject classSubject) {
        return ClassSubjectResponse.builder()
                .id(classSubject.getId())
                .subject(new SubjectResponse(
                        classSubject.getSubject().getId(),
                        classSubject.getSubject().getName()
                ))
                .classBlock(new ClassBlockResponses(
                        classSubject.getClassBlock().getId(),
                        classSubject.getClassBlock().getName(),
                        new ClassLevelResponse(
                                classSubject.getClassBlock().getClassLevel().getId()
                        )
                ))
                .academicYear(new AcademicSessionResponse(
                        classSubject.getAcademicYear().getId(),
                        classSubject.getAcademicYear().getSessionName().getName()
                ))
                .teacher(classSubject.getTeacher() != null ? new SubjectUserResponse(
                        classSubject.getTeacher().getId(),
                        classSubject.getTeacher().getUser().getFirstName(),
                        classSubject.getTeacher().getUser().getLastName(),
                        classSubject.getTeacher().getUser().getEmail()
                ) : null)
                .createdAt(classSubject.getCreatedAt())
                .updatedAt(classSubject.getUpdatedAt())
                .build();
    }
}