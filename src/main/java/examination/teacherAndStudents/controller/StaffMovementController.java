package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.StaffMovementRequest;
import examination.teacherAndStudents.dto.StaffMovementResponse;
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
    public ResponseEntity<ApiResponse<StaffMovementResponse>> createStaffMovement(@RequestBody StaffMovementRequest request) {
        StaffMovementResponse response = staffMovementService.createStaffMovement(request);
        ApiResponse<StaffMovementResponse> apiResponse = new ApiResponse<>("Staff movement created successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffMovementResponse>> editStaffMovement(@PathVariable Long id, @RequestBody StaffMovementRequest request) {
        StaffMovementResponse response = staffMovementService.editStaffMovement(id, request);
        ApiResponse<StaffMovementResponse> apiResponse = new ApiResponse<>("Staff movement updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaffMovement(@PathVariable Long id) {
        staffMovementService.deleteStaffMovement(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Staff movement deleted successfully", true, null);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffMovementResponse>>> getAllStaffMovements() {
        List<StaffMovementResponse> movements = staffMovementService.getAllStaffMovements();
        ApiResponse<List<StaffMovementResponse>> apiResponse = new ApiResponse<>("All staff movements fetched successfully", true, movements);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffMovementResponse>> getStaffMovementById(@PathVariable Long id) {
        StaffMovementResponse response = staffMovementService.getStaffMovementById(id);
        ApiResponse<StaffMovementResponse> apiResponse = new ApiResponse<>("Staff movement fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<StaffMovementResponse>> updateStaffMovementStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        StaffMovementResponse response = staffMovementService.updateStaffMovementStatus(id, status);
        ApiResponse<StaffMovementResponse> apiResponse = new ApiResponse<>("Staff movement status updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/update/{id}/status")
    public ResponseEntity<ApiResponse<StaffMovementResponse>> approveOrDeclineStaffMovement(
            @PathVariable Long id,
            @RequestParam String status) {
        StaffMovementResponse response = staffMovementService.approveOrDeclineStaffMovement(id, status);
        ApiResponse<StaffMovementResponse> apiResponse = new ApiResponse<>("Staff movement approval status updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }
}
