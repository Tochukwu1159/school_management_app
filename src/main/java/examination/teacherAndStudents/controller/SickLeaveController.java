package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.SickLeaveRequest;
import examination.teacherAndStudents.entity.SickLeave;
import examination.teacherAndStudents.service.SickLeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sick-leave")
public class SickLeaveController {

    private final SickLeaveService sickLeaveService;


    @PostMapping("/apply")
    public ResponseEntity<Void> applyForSickLeave(@RequestBody SickLeaveRequest sickLeaveRequest) {
        sickLeaveService.applyForSickLeave(sickLeaveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PutMapping("/update/{sickLeaveId}")
    public ResponseEntity<Void> updateSickLeave(@PathVariable Long sickLeaveId,
                                                @RequestBody SickLeaveRequest updatedSickLeave) {
        sickLeaveService.updateSickLeave(sickLeaveId, updatedSickLeave);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel/{sickLeaveId}")
    public ResponseEntity<Void> cancelSickLeave(@PathVariable Long sickLeaveId) {
        sickLeaveService.cancelSickLeave(sickLeaveId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("all")
    public ResponseEntity<List<SickLeave>> getAllSickLeave() {
        List<SickLeave> sickLeaveList = sickLeaveService.getAllSickLeave();
        return ResponseEntity.ok(sickLeaveList);
    }
}
