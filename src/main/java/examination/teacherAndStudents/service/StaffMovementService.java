package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StaffMovementRequest;
import examination.teacherAndStudents.dto.StaffMovementResponse;

import java.util.List;

public interface StaffMovementService {
    StaffMovementResponse createStaffMovement(StaffMovementRequest request);
    StaffMovementResponse editStaffMovement(Long id, StaffMovementRequest request);
    void deleteStaffMovement(Long id);
    List<StaffMovementResponse> getAllStaffMovements();
    StaffMovementResponse getStaffMovementById(Long id);
    StaffMovementResponse approveOrDeclineStaffMovement(Long id, String status);
}