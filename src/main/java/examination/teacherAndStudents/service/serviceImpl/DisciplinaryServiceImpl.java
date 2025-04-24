package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.entity.DisciplinaryAction;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.EntityNotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.DisciplinaryActionRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.service.DisciplinaryService;
import examination.teacherAndStudents.utils.DisciplinaryActionType;
import examination.teacherAndStudents.utils.ProfileStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Service implementation for managing disciplinary actions.
 */
@Service
@RequiredArgsConstructor
public class DisciplinaryServiceImpl implements DisciplinaryService {

    private static final Logger logger = LoggerFactory.getLogger(DisciplinaryServiceImpl.class);
    private final DisciplinaryActionRepository disciplinaryActionRepository;
    private final ProfileRepository profileRepository;

    /**
     * Issues a new disciplinary action for a profile.
     *
     * @param profile     The profile receiving the action.
     * @param issuedBy    The staff profile issuing the action.
     * @param actionType  The type of disciplinary action.
     * @param reason      The reason for the action.
     * @param description Detailed description of the action.
     * @param startDate   The start date of the action.
     * @param endDate     The end date of the action (nullable).
     * @return The saved DisciplinaryAction entity.
     * @throws BadRequestException if input parameters are invalid or conflicting actions exist.
     */
    @Transactional
    @Override
    public DisciplinaryAction issueDisciplinaryAction(
            Profile profile,
            Profile issuedBy,
            DisciplinaryActionType actionType,
            String reason,
            String description,
            LocalDate startDate,
            LocalDate endDate) {

        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile admin = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        validateDisciplinaryActionInput(profile, issuedBy, actionType, reason, startDate, endDate);

        // Check for conflicting actions
        if (actionType == DisciplinaryActionType.EXPULSION &&
                disciplinaryActionRepository.existsActiveActionForProfile(profile, DisciplinaryActionType.EXPULSION, LocalDate.now())) {
            throw new BadRequestException("Profile already has an active expulsion.");
        }
        if (actionType == DisciplinaryActionType.SUSPENSION &&
                disciplinaryActionRepository.existsActiveActionForProfile(profile, DisciplinaryActionType.SUSPENSION, LocalDate.now())) {
            throw new BadRequestException("Profile already has an active suspension.");
        }

        DisciplinaryAction action = DisciplinaryAction.builder()
                .profile(profile)
                .issuedBy(issuedBy)
                .actionType(actionType)
                .reason(reason)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .school(admin.getUser().getSchool())
                .active(true)
                .build();

        // Update profile status based on action type
        updateProfileStatusForAction(profile, actionType, endDate);
        profileRepository.save(profile);

        DisciplinaryAction savedAction = disciplinaryActionRepository.save(action);
        logger.info("Issued disciplinary action ID {} (type: {}) for profile ID {}",
                savedAction.getId(), actionType, profile.getId());
        return savedAction;
    }

    /**
     * Retrieves all active disciplinary actions for a given profile.
     *
     * @param profile The profile to query actions for.
     * @return List of active disciplinary actions.
     */
    @Override
    public List<DisciplinaryAction> getActiveActionsForProfile(Profile profile) {
        if (profile == null) {
            throw new BadRequestException("Profile cannot be null.");
        }
        return disciplinaryActionRepository.findByProfileAndActiveTrue(profile);
    }

    /**
     * Checks if a profile is currently suspended.
     *
     * @param profile The profile to check.
     * @return True if the profile has an active suspension, false otherwise.
     */
    @Override
    public boolean isProfileSuspended(Profile profile) {
        if (profile == null) {
            throw new BadRequestException("Profile cannot be null.");
        }
        return disciplinaryActionRepository.existsActiveActionForProfile(
                profile, DisciplinaryActionType.SUSPENSION, LocalDate.now());
    }

    /**
     * Deactivates all expired disciplinary actions.
     */
    @Transactional
    @Override
    public void deactivateExpiredActions() {
        List<DisciplinaryAction> expiredActions = disciplinaryActionRepository
                .findByActiveTrueAndEndDateBefore(LocalDate.now());

        for (DisciplinaryAction action : expiredActions) {
            action.setActive(false);
            Profile profile = action.getProfile();
            updateProfileStatusAfterActionChange(profile);
            profileRepository.save(profile);
        }
        disciplinaryActionRepository.saveAll(expiredActions);
        logger.info("Deactivated {} expired disciplinary actions.", expiredActions.size());
    }

    /**
     * Updates an existing disciplinary action.
     *
     * @param actionId    The ID of the action to update.
     * @param reason      The updated reason.
     * @param description The updated description.
     * @param endDate     The updated end date.
     * @param isActive    The updated active status.
     * @throws EntityNotFoundException if the action 🙂is not found.
     * @throws BadRequestException if the updated parameters are invalid.
     */
    @Transactional
    @Override
    public void updateDisciplinaryAction(Long actionId, String reason, String description,
                                         LocalDate endDate, boolean isActive) {
        if (actionId == null) {
            throw new BadRequestException("Action ID cannot be null.");
        }

        DisciplinaryAction action = disciplinaryActionRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Disciplinary action not found with id: " + actionId));

        validateUpdateInput(reason, endDate, action.getStartDate());

        action.setReason(reason);
        action.setDescription(description);
        action.setEndDate(endDate);
        action.setActive(isActive);

        // Update profile status based on action type and active status
        Profile profile = action.getProfile();
        if (!isActive) {
            updateProfileStatusAfterActionChange(profile);
        } else {
            updateProfileStatusForAction(profile, action.getActionType(), endDate);
        }
        profileRepository.save(profile);

        disciplinaryActionRepository.save(action);
        logger.info("Updated disciplinary action ID {}", actionId);
    }

    /**
     * Cancels a disciplinary action by setting it to inactive.
     *
     * @param actionId The ID of the action to cancel.
     * @throws EntityNotFoundException if the action is not found.
     */
    @Transactional
    @Override
    public void cancelDisciplinaryAction(Long actionId) {
        if (actionId == null) {
            throw new BadRequestException("Action ID cannot be null.");
        }

        DisciplinaryAction action = disciplinaryActionRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Disciplinary action not found with id: " + actionId));

        action.setActive(false);

        // Update profile status after cancellation
        Profile profile = action.getProfile();
        updateProfileStatusAfterActionChange(profile);
        profileRepository.save(profile);

        disciplinaryActionRepository.save(action);
        logger.info("Canceled disciplinary action ID {}", actionId);
    }

    /**
     * Retrieves a disciplinary action by its ID.
     *
     * @param id The ID of the disciplinary action.
     * @return The DisciplinaryAction entity.
     * @throws EntityNotFoundException if the action is not found.
     */
    @Override
    public DisciplinaryAction getDisciplinaryActionById(Long id) {
        if (id == null) {
            throw new BadRequestException("Action ID cannot be null.");
        }
        return disciplinaryActionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Disciplinary action not found with id: " + id));
    }

    /**
     * Retrieves all active disciplinary actions with pagination and filtering.
     *
     * @param pageable Pagination information.
     * @return Page of active disciplinary actions.
     */
    @Override
    public Page<DisciplinaryAction> getAllActiveDisciplinaryActions(Pageable pageable) {
        Page<DisciplinaryAction> actions = disciplinaryActionRepository.findByActiveTrue(pageable);
        return (Page<DisciplinaryAction>) actions.filter(action ->
                action.getStartDate().isBefore(LocalDate.now()) &&
                        (action.getEndDate() == null || action.getEndDate().isAfter(LocalDate.now())));
    }

    /**
     * Updates the profile status based on the disciplinary action type.
     *
     * @param profile    The profile to update.
     * @param actionType The type of disciplinary action.
     * @param endDate    The end date of the action.
     */
    private void updateProfileStatusForAction(Profile profile, DisciplinaryActionType actionType, LocalDate endDate) {
        switch (actionType) {
            case EXPULSION:
                profile.setProfileStatus(ProfileStatus.EXPELLED);
                profile.setSuspensionEndDate(null);
                break;
            case SUSPENSION:
                profile.setProfileStatus(ProfileStatus.SUSPENDED);
                profile.setSuspensionEndDate(endDate);
                break;
            case PROBATION:
                profile.setProfileStatus(ProfileStatus.ON_PROBATION);
                profile.setSuspensionEndDate(endDate);
                break;
            case DETENTION:
                profile.setProfileStatus(ProfileStatus.RESTRICTED);
                profile.setSuspensionEndDate(endDate);
                break;
            case WARNING:
                profile.setProfileStatus(ProfileStatus.WARNED);
                profile.setSuspensionEndDate(null);
                break;
            case FINE:
                profile.setProfileStatus(ProfileStatus.FINED);
                profile.setSuspensionEndDate(null);
                break;
            case COMMUNITY_SERVICE:
                profile.setProfileStatus(ProfileStatus.ON_COMMUNITY_SERVICE);
                profile.setSuspensionEndDate(endDate);
                break;
            default:
                profile.setProfileStatus(ProfileStatus.ACTIVE);
                profile.setSuspensionEndDate(null);
                logger.warn("Unknown DisciplinaryActionType: {}. Defaulting profile status to ACTIVE.", actionType);
        }
    }

    /**
     * Updates the profile status after an action is deactivated or canceled.
     *
     * @param profile The profile to update.
     */
    private void updateProfileStatusAfterActionChange(Profile profile) {
        List<DisciplinaryAction> activeActions = disciplinaryActionRepository.findByProfileAndActiveTrue(profile);

        // Check for the most severe active action
        if (activeActions.stream().anyMatch(a -> a.getActionType() == DisciplinaryActionType.EXPULSION)) {
            profile.setProfileStatus(ProfileStatus.EXPELLED);
            profile.setSuspensionEndDate(null);
        } else if (activeActions.stream().anyMatch(a -> a.getActionType() == DisciplinaryActionType.SUSPENSION)) {
            profile.setProfileStatus(ProfileStatus.SUSPENDED);
            activeActions.stream()
                    .filter(a -> a.getActionType() == DisciplinaryActionType.SUSPENSION)
                    .map(DisciplinaryAction::getEndDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .ifPresent(profile::setSuspensionEndDate);
        } else if (activeActions.stream().anyMatch(a -> a.getActionType() == DisciplinaryActionType.PROBATION)) {
            profile.setProfileStatus(ProfileStatus.ON_PROBATION);
            activeActions.stream()
                    .filter(a -> a.getActionType() == DisciplinaryActionType.PROBATION)
                    .map(DisciplinaryAction::getEndDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .ifPresent(profile::setSuspensionEndDate);
        } else if (activeActions.stream().anyMatch(a -> a.getActionType() == DisciplinaryActionType.DETENTION)) {
            profile.setProfileStatus(ProfileStatus.RESTRICTED);
            activeActions.stream()
                    .filter(a -> a.getActionType() == DisciplinaryActionType.DETENTION)
                    .map(DisciplinaryAction::getEndDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .ifPresent(profile::setSuspensionEndDate);
        } else if (activeActions.stream().anyMatch(a -> a.getActionType() == DisciplinaryActionType.WARNING)) {
            profile.setProfileStatus(ProfileStatus.WARNED);
            profile.setSuspensionEndDate(null);
        } else if (activeActions.stream().anyMatch(a -> a.getActionType() == DisciplinaryActionType.FINE)) {
            profile.setProfileStatus(ProfileStatus.FINED);
            profile.setSuspensionEndDate(null);
        } else if (activeActions.stream().anyMatch(a -> a.getActionType() == DisciplinaryActionType.COMMUNITY_SERVICE)) {
            profile.setProfileStatus(ProfileStatus.ON_COMMUNITY_SERVICE);
            activeActions.stream()
                    .filter(a -> a.getActionType() == DisciplinaryActionType.COMMUNITY_SERVICE)
                    .map(DisciplinaryAction::getEndDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .ifPresent(profile::setSuspensionEndDate);
        } else {
            profile.setProfileStatus(ProfileStatus.ACTIVE);
            profile.setSuspensionEndDate(null);
        }
    }

    /**
     * Validates input parameters for issuing a disciplinary action.
     */
    private void validateDisciplinaryActionInput(Profile profile, Profile issuedBy,
                                                 DisciplinaryActionType actionType, String reason,
                                                 LocalDate startDate, LocalDate endDate) {
        if (profile == null || issuedBy == null) {
            throw new BadRequestException("Profile and issuedBy cannot be null.");
        }
        if (actionType == null) {
            throw new BadRequestException("Action type cannot be null.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Reason cannot be null or empty.");
        }
        if (startDate == null) {
            throw new BadRequestException("Start date cannot be null.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past.");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date.");
        }
    }

    /**
     * Validates input parameters for updating a disciplinary action.
     */
    private void validateUpdateInput(String reason, LocalDate endDate, LocalDate startDate) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Reason cannot be null or empty.");
        }
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date.");
        }
    }
}