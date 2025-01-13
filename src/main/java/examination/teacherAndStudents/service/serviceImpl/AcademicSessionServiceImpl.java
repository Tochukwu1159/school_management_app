package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.AcademicSessionRequest;
import examination.teacherAndStudents.dto.AcademicSessionResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.StudentTermRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.AcademicSessionService;
import examination.teacherAndStudents.utils.EntityFetcher;
import examination.teacherAndStudents.utils.ProfileStatus;
import examination.teacherAndStudents.utils.Roles;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final EntityFetcher entityFetcher;

    public AcademicSessionResponse  createAcademicSession(AcademicSessionRequest request) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = entityFetcher.fetchLoggedInAdmin(email);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }

        // Check if the subscription has expired
        School school = admin.getSchool();
        AcademicSession session = AcademicSession.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
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

    public List<AcademicSessionResponse> getAllAcademicSessions() {
        return academicSessionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteAcademicSession(Long id) {
        academicSessionRepository.deleteById(id);
    }

    @Transactional
    public void graduateStudentsForSession(Long academicSessionId) {
        // Fetch the academic session
        AcademicSession session = entityFetcher.fetchAcademicSession(academicSessionId);

        // Check if the session has ended
        if (session.getEndDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Academic session has not ended yet");
        }
        // Fetch all SS3 profiles with 'ACTIVE' status
        List<Profile> secondaryProfilesToGraduate = profileRepository.findAllByClassBlockClassLevelClassNameAndProfileStatus(
                "SS3", ProfileStatus.ACTIVE);

        // Fetch all primary 6 profiles with 'ACTIVE' status
        List<Profile> primaryProfilesToGraduate = profileRepository.findAllByClassBlockClassLevelClassNameAndProfileStatus(
                "primary6", ProfileStatus.ACTIVE);

        if (secondaryProfilesToGraduate.isEmpty() || primaryProfilesToGraduate.isEmpty()) {
            System.out.println("No students found for graduation in session: " + session.getName());
            return; // Early return if no profiles found
        }
        // Update the status of secondary profiles to 'GRADUATED'
        secondaryProfilesToGraduate.forEach(profile -> profile.setProfileStatus(ProfileStatus.GRADUATED));

        // Update the status of primary profiles to 'GRADUATED'
        primaryProfilesToGraduate.forEach(profile -> profile.setProfileStatus(ProfileStatus.GRADUATED));

        // Batch update (save all at once)
        profileRepository.saveAll(secondaryProfilesToGraduate);
        profileRepository.saveAll(primaryProfilesToGraduate);

        System.out.println("Graduation process completed for session: " + session.getName());
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
                        .academicSession(session).build(),

                StudentTerm.builder().name("Second Term")
                        .startDate(academicSessionRequest.getSecondTermStartDate())
                        .endDate(academicSessionRequest.getSecondTermEndDate())
                        .academicSession(session).build(),

                StudentTerm.builder().name("Third Term")
                        .startDate(academicSessionRequest.getThirdTermStartDate())
                        .endDate(academicSessionRequest.getThirdTermEndDate())
                        .academicSession(session).build()
        );
        studentTermRepository.saveAll(terms);
    }

}
