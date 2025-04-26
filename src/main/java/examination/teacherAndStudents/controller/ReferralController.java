package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<UserPointsResponse>> getUserPoints(@RequestParam Long userId) {
        UserPointsResponse pointsResponse = referralService.getUserPoints(userId);
        ApiResponse<UserPointsResponse> response = new ApiResponse<>("User points fetched successfully", true, pointsResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<RedeemResponse>> redeemPoints(
            @RequestParam Long userId,
            @RequestBody RedeemPointsRequestDto request) {
        RedeemResponse redeemResponse = referralService.redeemPoints(userId, request);
        ApiResponse<RedeemResponse> response = new ApiResponse<>("Points redeemed successfully", true, redeemResponse);
        return ResponseEntity.ok(response);
    }
}
