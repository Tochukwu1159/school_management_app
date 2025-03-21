package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.StaffMovementRequest;
import examination.teacherAndStudents.dto.StaffMovementResponse;
import examination.teacherAndStudents.entity.StaffMovement;
import examination.teacherAndStudents.service.StaffMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff-movements")
@RequiredArgsConstructor
public class StaffMovementController {

    private final StaffMovementService staffMovementService;

    @PostMapping
    public ResponseEntity<StaffMovementResponse> createStaffMovement(@RequestBody StaffMovementRequest request) {
        return ResponseEntity.ok(staffMovementService.createStaffMovement(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffMovementResponse> editStaffMovement(@PathVariable Long id, @RequestBody StaffMovementRequest request) {
        return ResponseEntity.ok(staffMovementService.editStaffMovement(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaffMovement(@PathVariable Long id) {
        staffMovementService.deleteStaffMovement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<StaffMovementResponse>> getAllStaffMovements() {
        return ResponseEntity.ok(staffMovementService.getAllStaffMovements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffMovementResponse> getStaffMovementById(@PathVariable Long id) {
        return ResponseEntity.ok(staffMovementService.getStaffMovementById(id));
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<StaffMovementResponse> updateStaffMovementStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        StaffMovementResponse response = staffMovementService.updateStaffMovementStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update/{id}/status")
    public ResponseEntity<StaffMovementResponse> approveOrDeclineStaffMovement(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(staffMovementService.approveOrDeclineStaffMovement(id, status));
    }
}
