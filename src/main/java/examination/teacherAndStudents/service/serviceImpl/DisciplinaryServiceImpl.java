package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.DisciplinaryActionRequest;
import examination.teacherAndStudents.dto.DisciplinaryActionResponse;
import examination.teacherAndStudents.entity.DisciplinaryAction;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.repository.DisciplinaryActionRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.DisciplinaryService;
import examination.teacherAndStudents.utils.DisciplinaryActionType;
import examination.teacherAndStudents.utils.ProfileStatus;
import examination.teacherAndStudents.utils.Roles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DisciplinaryServiceImpl implements DisciplinaryService {

    private static final Logger logger = LoggerFactory.getLogger(DisciplinaryServiceImpl.class);
    private final DisciplinaryActionRepository disciplinaryActionRepository;
    private final ProfileRepository profileRepository;

    // Mapping of DisciplinaryActionType to ProfileStatus
    private static final Map<DisciplinaryActionType, ProfileStatus> ACTION_TYPE_TO_STATUS = Map.of(
            DisciplinaryActionType.EXPULSION, ProfileStatus.EXPELLED,
            DisciplinaryActionType.SUSPENSION, ProfileStatus.SUSPENDED,
            DisciplinaryActionType.PROBATION, ProfileStatus.ON_PROBATION,
            DisciplinaryActionType.DETENTION, ProfileStatus.RESTRICTED,
            DisciplinaryActionType.WARNING, ProfileStatus.WARNED,
            DisciplinaryActionType.FINE, ProfileStatus.FINED,
            DisciplinaryActionType.COMMUNITY_SERVICE, ProfileStatus.ON_COMMUNITY_SERVICE
    );

    // Priority for action types (higher number = more severe)
    private static final Map<DisciplinaryActionType, Integer> ACTION_TYPE_PRIORITY = Map.of(
            DisciplinaryActionType.EXPULSION, 7,
            DisciplinaryActionType.SUSPENSION, 6,
            DisciplinaryActionType.PROBATION, 5,
            DisciplinaryActionType.DETENTION, 4,
            DisciplinaryActionType.COMMUNITY_SERVICE, 3,
            DisciplinaryActionType.FINE, 2,
            DisciplinaryActionType.WARNING, 1
    );

    @Override
    @Transactional
    public DisciplinaryActionResponse issueDisciplinaryAction(@Valid DisciplinaryActionRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile issuer = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Issuer profile not found for email: " + email));

        if (!issuer.getUser().getRoles().contains(Roles.ADMIN) && !issuer.getUser().getRoles().contains(Roles.TEACHER)) {
            throw new BadRequestException("Only admins or teachers can issue disciplinary actions");
        }

        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with ID: " + request.getProfileId()));
        Profile issuedBy = profileRepository.findById(request.getIssuedById())
                .orElseThrow(() -> new EntityNotFoundException("Issued by profile not found with ID: " + request.getIssuedById()));

        validateDisciplinaryActionInput(request);

        // Check for conflicting actions
        if (request.getActionType() == DisciplinaryActionType.EXPULSION &&
                disciplinaryActionRepository.existsActiveActionForProfile(profile, DisciplinaryActionType.EXPULSION, LocalDate.now())) {
            throw new BadRequestException("Profile already has an active expulsion");
        }
        if (request.getActionType() == DisciplinaryActionType.SUSPENSION &&
                disciplinaryActionRepository.existsActiveActionForProfile(profile, DisciplinaryActionType.SUSPENSION, LocalDate.now())) {
            throw new BadRequestException("Profile already has an active suspension");
        }

        DisciplinaryAction action = DisciplinaryAction.builder()
                .profile(profile)
                .issuedBy(issuedBy)
                .actionType(request.getActionType())
                .reason(request.getReason())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .school(issuer.getUser().getSchool())
                .active(true)
                .build();

        updateProfileStatus(profile);
        profileRepository.save(profile);

        DisciplinaryAction savedAction = disciplinaryActionRepository.save(action);
        logger.info("Issued disciplinary action ID {} (type: {}) for profile ID {}", savedAction.getId(), request.getActionType(), profile.getId());
        return mapToResponse(savedAction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisciplinaryAction> getActiveActionsForProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with ID: " + profileId));
        if (profile == null) {
            throw new BadRequestException("Profile cannot be null");
        }
        return disciplinaryActionRepository.findByProfileAndActiveTrue(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProfileSuspended(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with ID: " + profileId));
        if (profile == null) {
            throw new BadRequestException("Profile cannot be null");
        }
        return disciplinaryActionRepository.existsActiveActionForProfile(profile, DisciplinaryActionType.SUSPENSION, LocalDate.now());
    }

    @Override
    @Transactional
    public void deactivateExpiredActions() {
        List<DisciplinaryAction> expiredActions = disciplinaryActionRepository.findByActiveTrueAndEndDateBefore(LocalDate.now());
        for (DisciplinaryAction action : expiredActions) {
            action.setActive(false);
            updateProfileStatus(action.getProfile());
            profileRepository.save(action.getProfile());
        }
        disciplinaryActionRepository.saveAll(expiredActions);
        logger.info("Deactivated {} expired disciplinary actions", expiredActions.size());
    }

    @Override
    @Transactional
    public void updateDisciplinaryAction(Long actionId, @Valid DisciplinaryActionRequest request) {
        DisciplinaryAction action = disciplinaryActionRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Disciplinary action not found with ID: " + actionId));

        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with ID: " + request.getProfileId()));
        Profile issuedBy = profileRepository.findById(request.getIssuedById())
                .orElseThrow(() -> new EntityNotFoundException("Issued by profile not found with ID: " + request.getIssuedById()));

        validateDisciplinaryActionInput(request);

        action.setProfile(profile);
        action.setIssuedBy(issuedBy);
        action.setActionType(request.getActionType());
        action.setReason(request.getReason());
        action.setDescription(request.getDescription());
        action.setStartDate(request.getStartDate());
        action.setEndDate(request.getEndDate());
        action.setActive(true);

        updateProfileStatus(profile);
        profileRepository.save(profile);

        disciplinaryActionRepository.save(action);
        logger.info("Updated disciplinary action ID {}", actionId);
    }

    @Override
    @Transactional
    public void cancelDisciplinaryAction(Long actionId) {
        DisciplinaryAction action = disciplinaryActionRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Disciplinary action not found with ID: " + actionId));

        action.setActive(false);
        updateProfileStatus(action.getProfile());
        profileRepository.save(action.getProfile());

        disciplinaryActionRepository.save(action);
        logger.info("Cancelled disciplinary action ID {}", actionId);
    }

    @Override
    @Transactional(readOnly = true)
    public DisciplinaryActionResponse getDisciplinaryActionById(Long id) {
        DisciplinaryAction action = disciplinaryActionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Disciplinary action not found with ID: " + id));
        return mapToResponse(action);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisciplinaryActionResponse> getAllActiveDisciplinaryActions(Pageable pageable) {
        return disciplinaryActionRepository.findByActiveTrueAndValidDate(LocalDate.now(), pageable)
                .map(this::mapToResponse);
    }

    private void updateProfileStatus(Profile profile) {
        List<DisciplinaryAction> activeActions = disciplinaryActionRepository.findByProfileAndActiveTrue(profile);
        if (activeActions.isEmpty()) {
            profile.setProfileStatus(ProfileStatus.ACTIVE);
            profile.setSuspensionEndDate(null);
            return;
        }

        // Find the most severe active action
        Optional<DisciplinaryAction> mostSevereAction = activeActions.stream()
                .max(Comparator.comparing(action -> ACTION_TYPE_PRIORITY.getOrDefault(action.getActionType(), 0)));

        if (mostSevereAction.isPresent()) {
            DisciplinaryAction action = mostSevereAction.get();
            profile.setProfileStatus(ACTION_TYPE_TO_STATUS.getOrDefault(action.getActionType(), ProfileStatus.ACTIVE));
            profile.setSuspensionEndDate(action.getEndDate());
        } else {
            profile.setProfileStatus(ProfileStatus.ACTIVE);
            profile.setSuspensionEndDate(null);
        }
    }

    private void validateDisciplinaryActionInput(DisciplinaryActionRequest request) {
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("Reason cannot be null or empty");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Description cannot be null or empty");
        }
        if (request.getStartDate() == null) {
            throw new BadRequestException("Start date cannot be null");
        }
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
        // Allow past start dates with a warning
        if (request.getStartDate().isBefore(LocalDate.now())) {
            logger.warn("Disciplinary action start date is in the past: {}", request.getStartDate());
        }
    }

    private DisciplinaryActionResponse mapToResponse(DisciplinaryAction action) {
        DisciplinaryActionResponse response = new DisciplinaryActionResponse();
        response.setId(action.getId());
        response.setProfileId(action.getProfile().getId());
        response.setProfileName(action.getProfile().getUser().getFirstName() + " " + action.getProfile().getUser().getLastName());
        response.setIssuedById(action.getIssuedBy().getId());
        response.setIssuedByName(action.getIssuedBy().getUser().getFirstName() + " " + action.getIssuedBy().getUser().getLastName());
        response.setActionType(action.getActionType());
        response.setReason(action.getReason());
        response.setDescription(action.getDescription());
        response.setStartDate(action.getStartDate());
        response.setEndDate(action.getEndDate());
        response.setActive(action.isActive());
        return response;
    }
}