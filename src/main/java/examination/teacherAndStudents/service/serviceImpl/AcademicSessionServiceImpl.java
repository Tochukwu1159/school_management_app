package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.AcademicSessionRequest;
import examination.teacherAndStudents.dto.AcademicSessionResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.StudentTermRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.AcademicSessionService;
import examination.teacherAndStudents.utils.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AcademicSessionServiceImpl implements AcademicSessionService {

    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final ProfileRepository profileRepository;
    private final EntityFetcher entityFetcher;
    private final UserRepository userRepository;

    public AcademicSessionResponse  createAcademicSession(AcademicSessionRequest request) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = entityFetcher.fetchLoggedInAdmin(email);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }

        // Check if the subscription has expired
        AcademicSession session = AcademicSession.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SessionStatus.ACTIVE)
                .resultReadyDate(request.getResultReadyDate())
                .school(admin.getSchool())
                .build();
        AcademicSession savedSession = academicSessionRepository.save(session);
        createStudentTerms(request,savedSession);
        return mapToResponse(savedSession);
    }

    public AcademicSessionResponse updateAcademicSession(Long id, AcademicSessionRequest request) {
        AcademicSession session = entityFetcher.fetchAcademicSession(id);
        session.setName(request.getName());
        session.setStartDate(request.getStartDate());
        session.setEndDate(request.getEndDate());
        AcademicSession updatedSession = academicSessionRepository.save(session);
        return mapToResponse(updatedSession);
    }

    public AcademicSessionResponse getAcademicSessionById(Long id) {
        AcademicSession session = entityFetcher.fetchAcademicSession(id);
        return mapToResponse(session);
    }

    public Page<AcademicSessionResponse> getAllAcademicSessions(
            String name,
            SessionStatus status,
            SessionPromotion promotion,
            Long id,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Admin not found"));

            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Fetch filtered academic sessions
            Page<AcademicSession> sessionsPage = academicSessionRepository.findAllWithFilters(
                    admin.getSchool().getId(),
                    name,
                    status,
                    promotion,
                    id,
                    pageable);

            // Map to response DTO
            return sessionsPage.map(this::mapToResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching academic sessions: " + e.getMessage());
        }
    }

    public void deleteAcademicSession(Long id) {
        academicSessionRepository.deleteById(id);
    }

    @Transactional
    public void graduateStudentsForSession(Long academicSessionId, List<Long> classBlockIds) {
        // Fetch and validate the academic session
        AcademicSession session = academicSessionRepository.findById(academicSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + academicSessionId));

        // Get authenticated admin user and school
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        School school = admin.getSchool();

        // Validate session end date
        if (session.getEndDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Academic session has not ended yet");
        }

        // Fetch all active profiles in the specified class blocks for this session and school
        List<Profile> profilesToGraduate = profileRepository.findByClassBlockIdInAndClassBlockClassLevelAcademicYearAndClassBlockClassLevelSchoolAndProfileStatus(
                classBlockIds,
                session,
                school,
                ProfileStatus.ACTIVE
        );

        if (profilesToGraduate.isEmpty()) {
            return;
        }

        // Update status to GRADUATED
        profilesToGraduate.forEach(profile -> {
            profile.setProfileStatus(ProfileStatus.GRADUATED);
        });

        // Batch save all updated profiles
        profileRepository.saveAll(profilesToGraduate);

        session.setSessionPromotion(SessionPromotion.SUCCESS);
        academicSessionRepository.save(session);
    }

    private AcademicSessionResponse mapToResponse(AcademicSession session) {
        return AcademicSessionResponse.builder()
                .id(session.getId())
                .name(session.getName())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .build();
    }

    private void createStudentTerms(AcademicSessionRequest academicSessionRequest, AcademicSession session) {

        List<StudentTerm> terms = Arrays.asList(
                StudentTerm.builder().name("First Term")
                        .startDate(academicSessionRequest.getFirstTermStartDate())
                        .endDate(academicSessionRequest.getFirstTermEndDate())
                        .termStatus(TermStatus.ACTIVE)
                        .resultReadyDate(academicSessionRequest.getFirstTermResultReadyDate())
                        .academicSession(session).build(),

                StudentTerm.builder().name("Second Term")
                        .startDate(academicSessionRequest.getSecondTermStartDate())
                        .termStatus(TermStatus.ACTIVE)
                        .endDate(academicSessionRequest.getSecondTermEndDate())
                        .resultReadyDate(academicSessionRequest.getSecondTermResultReadyDate())
                        .academicSession(session).build(),

                StudentTerm.builder().name("Third Term")
                        .startDate(academicSessionRequest.getThirdTermStartDate())
                        .endDate(academicSessionRequest.getThirdTermEndDate())
                        .termStatus(TermStatus.ACTIVE)
                        .resultReadyDate(academicSessionRequest.getThirdTermResultReadyDate())
                        .academicSession(session).build()
        );
        studentTermRepository.saveAll(terms);
    }

}
