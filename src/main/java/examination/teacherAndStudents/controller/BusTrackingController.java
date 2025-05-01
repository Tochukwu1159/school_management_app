package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.BusTrackingResponse;
import examination.teacherAndStudents.service.BusTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bus")
@RequiredArgsConstructor
public class BusTrackingController {

    private final BusTrackingService busTrackingService;

    @GetMapping("/track")
    public ResponseEntity<ApiResponse<BusTrackingResponse>> trackBus() {
        BusTrackingResponse response = busTrackingService.trackBus();
        return ResponseEntity.ok(new ApiResponse<>("Bus tracking data retrieved successfully ", true, response));
    }
}