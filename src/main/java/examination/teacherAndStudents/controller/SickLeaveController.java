package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.SickLeaveCancelRequest;
import examination.teacherAndStudents.dto.SickLeaveRequest;
import examination.teacherAndStudents.dto.SickLeaveRequestDto;
import examination.teacherAndStudents.entity.Leave;
import examination.teacherAndStudents.service.SickLeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/leave")
public class SickLeaveController {

    private final SickLeaveService sickLeaveService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<String>> applyForSickLeave(@RequestBody SickLeaveRequest sickLeaveRequest) {
        String response = sickLeaveService.applyForSickLeave(sickLeaveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("Sick leave applied successfully", true, response));
    }

    @PutMapping("/approve-reject/{sickLeaveId}")
    public ResponseEntity<ApiResponse<String>> updateSickLeave(@PathVariable Long sickLeaveId,
                                                               @RequestBody SickLeaveRequestDto updatedSickLeave) {
        String responseMessage = sickLeaveService.processSickLeaveRequest(sickLeaveId, updatedSickLeave);
        return ResponseEntity.ok(new ApiResponse<>("Sick leave status updated", true, responseMessage));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<String>> cancelSickLeave(@RequestBody SickLeaveCancelRequest cancelRequest) {
        String result = sickLeaveService.cancelSickLeave(cancelRequest);
        return ResponseEntity.ok(new ApiResponse<>("Sick leave cancelled successfully", true, result));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Leave>>> getAllSickLeave() {
        List<Leave> sickLeaveList = sickLeaveService.getAllSickLeaves();
        return ResponseEntity.ok(new ApiResponse<>("List of all sick leaves", true, sickLeaveList));
    }
}
