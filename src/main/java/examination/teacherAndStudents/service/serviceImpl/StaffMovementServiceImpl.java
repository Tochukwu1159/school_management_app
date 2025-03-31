package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.StaffMovementRequest;
import examination.teacherAndStudents.dto.StaffMovementResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.StaffMovement;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.StaffMovementRepository;
import examination.teacherAndStudents.service.StaffMovementService;
import examination.teacherAndStudents.utils.EntityFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StaffMovementServiceImpl implements StaffMovementService {

    private static final String MOVEMENT_NOT_FOUND = "StaffMovement with id %d not found";
    private static final String INVALID_STATUS = "Status must be either APPROVED or REJECTED";
    private static final String INVALID_TRANSITION = "Staff movement must be marked as RETURNED before it can be VERIFIED";
    private static final String INVALID_STATUS_UPDATE = "Invalid status update for staff movement";

    private final StaffMovementRepository staffMovementRepository;
    private final EntityFetcher entityFetcher;

    @Override
    @Transactional
    public StaffMovementResponse createStaffMovement(StaffMovementRequest request) {
        log.info("Creating new staff movement for purpose: {}", request.getPurpose());

        User loggedInUser = getAuthenticatedUser();
        Profile profile = entityFetcher.fetchProfileByUser(loggedInUser);

        StaffMovement staffMovement = buildNewStaffMovement(request, profile);
        staffMovement = staffMovementRepository.save(staffMovement);

        log.info("Created staff movement with ID: {}", staffMovement.getId());
        return toResponse(staffMovement);
    }

    @Override
    @Transactional
    public StaffMovementResponse editStaffMovement(Long id, StaffMovementRequest request) {
        log.info("Editing staff movement with ID: {}", id);

        StaffMovement staffMovement = findStaffMovementById(id);
        updateStaffMovementDetails(staffMovement, request);
        staffMovement = staffMovementRepository.save(staffMovement);

        return toResponse(staffMovement);
    }

    @Override
    @Transactional
    public void deleteStaffMovement(Long id) {
        log.info("Deleting staff movement with ID: {}", id);

        if (!staffMovementRepository.existsById(id)) {
            throw new NotFoundException(String.format(MOVEMENT_NOT_FOUND, id));
        }
        staffMovementRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffMovementResponse> getAllStaffMovements() {
        log.info("Fetching all staff movements");
        return staffMovementRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffMovementResponse getStaffMovementById(Long id) {
        log.info("Fetching staff movement with ID: {}", id);
        return toResponse(findStaffMovementById(id));
    }

    @Override
    @Transactional
    public StaffMovementResponse approveOrDeclineStaffMovement(Long id, String status) {
        log.info("Processing approval for staff movement ID: {} with status: {}", id, status);

        validateApprovalStatus(status);
        User loggedInUser = getAuthenticatedUser();
        Profile profile = entityFetcher.fetchProfileByUser(loggedInUser);
        StaffMovement staffMovement = findStaffMovementById(id);

        updateApprovalStatus(staffMovement, status, profile);
        staffMovement = staffMovementRepository.save(staffMovement);

        return toResponse(staffMovement);
    }

    @Override
    @Transactional
    public StaffMovementResponse updateStaffMovementStatus(Long id, String status) {
        log.info("Updating status for staff movement ID: {} to {}", id, status);

        StaffMovement staffMovement = findStaffMovementById(id);
        User loggedInUser = getAuthenticatedUser();
        Profile profile = entityFetcher.fetchProfileByUser(loggedInUser);

        updateMovementStatus(staffMovement, status, profile);
        staffMovement = staffMovementRepository.save(staffMovement);

        return toResponse(staffMovement);
    }

    // Helper methods
    private User getAuthenticatedUser() {
        String email = entityFetcher.fetchLoggedInUser();
        return entityFetcher.fetchUserFromEmail(email);
    }

    private StaffMovement findStaffMovementById(Long id) {
        return staffMovementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(MOVEMENT_NOT_FOUND, id)));
    }

    private StaffMovement buildNewStaffMovement(StaffMovementRequest request, Profile profile) {
        return StaffMovement.builder()
                .staff(profile)
                .purpose(request.getPurpose())
                .status(StaffMovement.Status.PENDING)
                .expectedReturnTime(request.getExpectedReturnTime())
                .build();
    }

    private void updateStaffMovementDetails(StaffMovement staffMovement, StaffMovementRequest request) {
        staffMovement.setPurpose(request.getPurpose());
        staffMovement.setExpectedReturnTime(request.getExpectedReturnTime());
    }

    private void validateApprovalStatus(String status) {
        if (!"APPROVED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException(INVALID_STATUS);
        }
    }

    private void updateApprovalStatus(StaffMovement staffMovement, String status, Profile approver) {
        staffMovement.setApprovedBy(approver);
        staffMovement.setStatus(StaffMovement.Status.valueOf(status.toUpperCase()));
    }

    private void updateMovementStatus(StaffMovement staffMovement, String status, Profile profile) {
        StaffMovement.Status newStatus = StaffMovement.Status.valueOf(status);

        switch (newStatus) {
            case RETURNED:
                staffMovement.setActualReturnTime(LocalDateTime.now());
                staffMovement.setStatus(StaffMovement.Status.RETURNED);
                break;
            case VERIFIED:
                validateReturnedStatus(staffMovement);
                staffMovement.setStatus(StaffMovement.Status.VERIFIED);
                staffMovement.setVerifiedBy(profile);
                break;
            default:
                throw new IllegalArgumentException(INVALID_STATUS_UPDATE);
        }
    }

    private void validateReturnedStatus(StaffMovement staffMovement) {
        if (!StaffMovement.Status.RETURNED.equals(staffMovement.getStatus())) {
            throw new IllegalStateException(INVALID_TRANSITION);
        }
    }

    private StaffMovementResponse toResponse(StaffMovement staffMovement) {
        return StaffMovementResponse.builder()
                .id(staffMovement.getId())
                .purpose(staffMovement.getPurpose())
                .expectedReturnTime(staffMovement.getExpectedReturnTime())
                .actualReturnTime(staffMovement.getActualReturnTime())
                .status(staffMovement.getStatus().name())
                .approvedBy(staffMovement.getApprovedBy() != null ?
                        staffMovement.getApprovedBy().getUser().getFirstName() + " " + staffMovement.getApprovedBy().getUser().getLastName() : null)
                .verifiedBy(staffMovement.getVerifiedBy() != null ?
                        staffMovement.getVerifiedBy().getUser().getFirstName() + " " +staffMovement.getVerifiedBy().getUser().getLastName(): null)
                .staffName(staffMovement.getStaff().getUser().getFirstName() + " " +staffMovement.getStaff().getUser().getLastName())
                .build();
    }
}