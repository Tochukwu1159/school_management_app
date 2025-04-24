package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.RedeemPointsRequestDto;
import examination.teacherAndStudents.dto.RedeemResponse;
import examination.teacherAndStudents.dto.UserPointsResponse;

public interface ReferralService {
    RedeemResponse redeemPoints(Long userId, RedeemPointsRequestDto request);
    UserPointsResponse getUserPoints(Long userId);
}
