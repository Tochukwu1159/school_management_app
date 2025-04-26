package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.StaffLevelRequest;
import examination.teacherAndStudents.dto.StaffLevelResponse;
import examination.teacherAndStudents.service.StaffLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff-levels")
@RequiredArgsConstructor
public class StaffLevelController {

    private final StaffLevelService staffLevelService;

    @PostMapping
    public ResponseEntity<ApiResponse<StaffLevelResponse>> createStaffLevel(@RequestBody StaffLevelRequest request) {
        StaffLevelResponse response = staffLevelService.createStaffLevel(request);
        ApiResponse<StaffLevelResponse> apiResponse = new ApiResponse<>("Staff level created successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffLevelResponse>> editStaffLevel(@PathVariable Long id, @RequestBody StaffLevelRequest request) {
        StaffLevelResponse response = staffLevelService.editStaffLevel(id, request);
        ApiResponse<StaffLevelResponse> apiResponse = new ApiResponse<>("Staff level updated successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaffLevel(@PathVariable Long id) {
        staffLevelService.deleteStaffLevel(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>("Staff level deleted successfully", true, null);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffLevelResponse>>> getAllStaffLevels() {
        List<StaffLevelResponse> responseList = staffLevelService.getAllStaffLevels();
        ApiResponse<List<StaffLevelResponse>> apiResponse = new ApiResponse<>("All staff levels fetched successfully", true, responseList);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffLevelResponse>> getStaffLevelById(@PathVariable Long id) {
        StaffLevelResponse response = staffLevelService.getStaffLevelById(id);
        ApiResponse<StaffLevelResponse> apiResponse = new ApiResponse<>("Staff level fetched successfully", true, response);
        return ResponseEntity.ok(apiResponse);
    }
}
