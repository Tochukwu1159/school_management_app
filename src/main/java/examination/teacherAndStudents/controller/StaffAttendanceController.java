package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.service.StaffAttendanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attendance")
@AllArgsConstructor
public class StaffAttendanceController {

    private final StaffAttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(
            @RequestParam("thumbprint") MultipartFile thumbprintFile,
            @RequestParam("deviceId") String deviceId) {

        try {
            attendanceService.checkIn(thumbprintFile.getBytes(), deviceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(
            @RequestParam("thumbprint") MultipartFile thumbprintFile,
            @RequestParam("deviceId") String deviceId) {

        try {
            attendanceService.checkOut(thumbprintFile.getBytes(), deviceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}