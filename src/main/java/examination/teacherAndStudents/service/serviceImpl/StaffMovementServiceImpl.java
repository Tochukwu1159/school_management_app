// SERVICE IMPL
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffMovementServiceImpl implements StaffMovementService {

    private final StaffMovementRepository staffMovementRepository;
    private final EntityFetcher entityFetcher;

    @Override
    public StaffMovementResponse createStaffMovement(StaffMovementRequest request) {
        String email = entityFetcher.fetchLoggedInUser();
        User loggedInUser =entityFetcher.fetchUserFromEmail(email);
        Profile profile = entityFetcher.fetchProfileByUser(loggedInUser);
        StaffMovement staffMovement = StaffMovement.builder()
                .staff(profile)
                .purpose(request.getPurpose())
                .status(StaffMovement.Status.PENDING)
                .expectedReturnTime(request.getExpectedReturnTime())
                .build();

        staffMovement = staffMovementRepository.save(staffMovement);

        return toResponse(staffMovement);
    }

    @Override
    public StaffMovementResponse editStaffMovement(Long id, StaffMovementRequest request) {
        StaffMovement staffMovement = staffMovementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StaffMovement with id " + id + " not found"));

        staffMovement.setPurpose(request.getPurpose());
        staffMovement.setExpectedReturnTime(request.getExpectedReturnTime());

        staffMovement = staffMovementRepository.save(staffMovement);

        return toResponse(staffMovement);
    }

    @Override
    public void deleteStaffMovement(Long id) {
        if (!staffMovementRepository.existsById(id)) {
            throw new NotFoundException("StaffMovement with id " + id + " not found");
        }
        staffMovementRepository.deleteById(id);
    }

    @Override
    public List<StaffMovementResponse> getAllStaffMovements() {
        return staffMovementRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StaffMovementResponse getStaffMovementById(Long id) {
        StaffMovement staffMovement = staffMovementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StaffMovement with id " + id + " not found"));

        return toResponse(staffMovement);
    }

    @Override
    public StaffMovementResponse approveOrDeclineStaffMovement(Long id, String status) {

        String email = entityFetcher.fetchLoggedInUser();
        User loggedInUser =entityFetcher.fetchUserFromEmail(email);
        Profile profile = entityFetcher.fetchProfileByUser(loggedInUser);
        StaffMovement staffMovement = staffMovementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Staff Movement with id " + id + " not found"));

        if (!"APPROVED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Status must be either APPROVED or REJECTED");
        }

        staffMovement.setApprovedBy(profile);
        staffMovement.setStatus(StaffMovement.Status.valueOf(status));
        staffMovement.setStatus(StaffMovement.Status.valueOf(status.toUpperCase()));

        staffMovement = staffMovementRepository.save(staffMovement);
        return toResponse(staffMovement);
    }

    private StaffMovementResponse toResponse(StaffMovement staffMovement) {
        return StaffMovementResponse.builder()
                .id(staffMovement.getId())
                .purpose(staffMovement.getPurpose())
                .expectedReturnTime(staffMovement.getExpectedReturnTime())
                .actualReturnTime(staffMovement.getActualReturnTime())
                .build();
    }
}