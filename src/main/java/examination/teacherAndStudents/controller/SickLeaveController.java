package examination.teacherAndStudents.controller;

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
    public ResponseEntity<String> applyForSickLeave(@RequestBody SickLeaveRequest sickLeaveRequest) {
      String response =  sickLeaveService.applyForSickLeave(sickLeaveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/approve-reject/{sickLeaveId}")
    public ResponseEntity<String> updateSickLeave(@PathVariable Long sickLeaveId,
                                                  @RequestBody SickLeaveRequestDto updatedSickLeave) {
        String responseMessage = sickLeaveService.processSickLeaveRequest(sickLeaveId, updatedSickLeave);
        return ResponseEntity.ok(responseMessage);
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelSickLeave(@RequestBody SickLeaveCancelRequest cancelRequest) {
        String result = sickLeaveService.cancelSickLeave(cancelRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("all")
    public ResponseEntity<List<Leave>> getAllSickLeave() {
        List<Leave> sickLeaveList = sickLeaveService.getAllSickLeaves();
        return ResponseEntity.ok(sickLeaveList);
    }
}
