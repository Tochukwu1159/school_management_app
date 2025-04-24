package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.RedeemPointsRequestDto;
import examination.teacherAndStudents.dto.RedeemResponse;
import examination.teacherAndStudents.dto.UserPointsResponse;
import examination.teacherAndStudents.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/referrals")
@RequiredArgsConstructor
public class ReferralController {
    private final ReferralService referralService;

    @GetMapping("/points")
    public ResponseEntity<UserPointsResponse> getUserPoints(@RequestParam Long userId) {
        return ResponseEntity.ok(referralService.getUserPoints(userId));
    }

    @PostMapping("/redeem")
    public ResponseEntity<RedeemResponse> redeemPoints(
            @RequestParam Long userId,
            @RequestBody RedeemPointsRequestDto request) {
        return ResponseEntity.ok(referralService.redeemPoints(userId, request));
    }
}
