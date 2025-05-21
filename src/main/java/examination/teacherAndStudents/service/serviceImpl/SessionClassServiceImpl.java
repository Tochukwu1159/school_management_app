package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SessionClassRequest;
import examination.teacherAndStudents.dto.SessionClassResponse;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.Assignment;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.SessionClass;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.UnauthorizedException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.AssignmentRepository;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.SessionClassRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.SessionClassService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionClassServiceImpl implements SessionClassService {

    private final SessionClassRepository sessionClassRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final ClassBlockRepository classBlockRepository;
    private final ProfileRepository profileRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SessionClassResponse addProfilesToSessionClass(SessionClassRequest request) {
        verifyAdminAccess();

        AcademicSession session = academicSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomNotFoundException("Academic Session not found with ID: " + request.getSessionId()));

        ClassBlock classBlock = classBlockRepository.findById(request.getClassBlockId())
                .orElseThrow(() -> new CustomNotFoundException("Class Block not found with ID: " + request.getClassBlockId()));

        Set<Profile> profiles = request.getProfileIds().stream()
                .map(profileId -> profileRepository.findById(profileId)
                        .orElseThrow(() -> new CustomNotFoundException("Profile not found with ID: " + profileId)))
                .filter(profile -> {
                    User user = userRepository.findById(profile.getUser().getId())
                            .orElseThrow(() -> new CustomNotFoundException("User not found for profile ID: " + profile.getId()));
                    return user.getRoles().contains(Roles.STUDENT);
                })
                .collect(Collectors.toSet());

        if (profiles.isEmpty()) {
            throw new IllegalArgumentException("At least one valid student profile must be provided");
        }

        // Check for existing SessionClass
        SessionClass sessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(request.getSessionId(), request.getClassBlockId())
                .orElseGet(() -> {
                    // Create new SessionClass if none exists
                    SessionClass newSessionClass = SessionClass.builder()
                            .academicSession(session)
                            .classBlock(classBlock)
                            .profiles(new HashSet<>())
                            .numberOfProfiles(0)
                            .build();
                    return sessionClassRepository.save(newSessionClass);
                });

        // Add new profiles
        profiles.forEach(sessionClass::addProfile);
        sessionClassRepository.save(sessionClass);

        return mapToResponse(sessionClass);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionClassResponse getSessionClassById(Long id) {
        verifyAdminAccess();

        SessionClass sessionClass = sessionClassRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Session Class not found with ID: " + id));

        return mapToResponse(sessionClass);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionClassResponse> getAllSessionClasses(
            Long sessionId, Long classBlockId, int page, int size, String sortBy, String sortDirection) {
        verifyAdminAccess();

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SessionClass> sessionClasses = sessionClassRepository.findAllWithFilters(
                sessionId, classBlockId, pageable);

        return sessionClasses.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public SessionClassResponse updateSessionClass(Long id, SessionClassRequest request) {
        verifyAdminAccess();

        SessionClass sessionClass = sessionClassRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Session Class not found with ID: " + id));

        AcademicSession session = academicSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomNotFoundException("Academic Session not found with ID: " + request.getSessionId()));

        ClassBlock classBlock = classBlockRepository.findById(request.getClassBlockId())
                .orElseThrow(() -> new CustomNotFoundException("Class Block not found with ID: " + request.getClassBlockId()));

        Set<Profile> profiles = request.getProfileIds().stream()
                .map(profileId -> profileRepository.findById(profileId)
                        .orElseThrow(() -> new CustomNotFoundException("Profile not found with ID: " + profileId)))
                .filter(profile -> {
                    User user = userRepository.findById(profile.getUser().getId())
                            .orElseThrow(() -> new CustomNotFoundException("User not found for profile ID: " + profile.getId()));
                    return user.getRoles().contains(Roles.STUDENT);
                })
                .collect(Collectors.toSet());

        if (profiles.isEmpty()) {
            throw new IllegalArgumentException("At least one valid student profile must be provided");
        }

        sessionClass.setAcademicSession(session);
        sessionClass.setClassBlock(classBlock);
        sessionClass.setProfiles(profiles);

        sessionClassRepository.save(sessionClass);

        return mapToResponse(sessionClass);
    }

    @Override
    @Transactional
    public void deleteSessionClass(Long id) {
        verifyAdminAccess();

        SessionClass sessionClass = sessionClassRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Session Class not found with ID: " + id));

        sessionClassRepository.delete(sessionClass);
    }

    @Override
    @Transactional
    public SessionClassResponse assignAssignmentToSessionClass(Long sessionClassId, Long assignmentId) {
        verifyAdminAccess();

        SessionClass sessionClass = sessionClassRepository.findById(sessionClassId)
                .orElseThrow(() -> new CustomNotFoundException("Session Class not found with ID: " + sessionClassId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomNotFoundException("Assignment not found with ID: " + assignmentId));

        if (!assignment.getClassBlocks().contains(sessionClass.getClassBlock())) {
            throw new IllegalArgumentException("Assignment with ID: " + assignmentId + " is not associated with the class block of Session Class ID: " + sessionClassId);
        }

        sessionClass.addAssignment(assignment);
        sessionClassRepository.save(sessionClass);

        return mapToResponse(sessionClass);
    }

    private SessionClassResponse mapToResponse(SessionClass sessionClass) {
        return SessionClassResponse.builder()
                .id(sessionClass.getId())
                .sessionId(sessionClass.getAcademicSession().getId())
                .classBlockId(sessionClass.getClassBlock().getId())
                .profileIds(sessionClass.getProfiles().stream().map(Profile::getId).collect(Collectors.toSet()))
                .numberOfProfiles(sessionClass.getNumberOfProfiles())
                .createdAt(sessionClass.getCreatedAt())
                .updatedAt(sessionClass.getUpdatedAt())
                .build();
    }

    private void verifyAdminAccess() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found with email: " + email));
        if (!user.getRoles().contains(Roles.ADMIN)) {
            log.warn("Unauthorized access attempt by user: {}", email);
            throw new UnauthorizedException("Access restricted to ADMIN role");
        }
        if (user.getSchool() == null) {
            log.warn("User {} not associated with a school", email);
            throw new CustomNotFoundException("User not associated with a school");
        }
    }
}