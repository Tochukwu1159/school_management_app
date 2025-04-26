package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.RedeemPointsRequestDto;
import examination.teacherAndStudents.dto.RedeemResponse;
import examination.teacherAndStudents.dto.UserPointsResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.BadRequestException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.RedemptionRepository;
import examination.teacherAndStudents.repository.UserPointsRepository;
import examination.teacherAndStudents.repository.WalletRepository;
import examination.teacherAndStudents.service.ReferralService;
import examination.teacherAndStudents.utils.RedemptionStatus;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReferralServiceImpl implements ReferralService {

    private final ProfileRepository profileRepository;
    private final UserPointsRepository userPointsRepository;
    private final RedemptionRepository redemptionRepository;
    private final WalletRepository walletRepository;

    public ReferralServiceImpl(ProfileRepository profileRepository, UserPointsRepository userPointsRepository, RedemptionRepository redemptionRepository, WalletRepository walletRepository) {
        this.profileRepository = profileRepository;
        this.userPointsRepository = userPointsRepository;
        this.redemptionRepository = redemptionRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public RedeemResponse redeemPoints(Long userId, RedeemPointsRequestDto request) {

//        String email = SecurityConfig.getAuthenticatedUserEmail();
//        Profile profile = profileRepository.findByUserEmail(email)
//                .orElseThrow(() -> new CustomNotFoundException("Please login"));

        Profile profile = profileRepository.findById(userId)
              .orElseThrow(() -> new CustomNotFoundException("Please login"));

        Wallet wallet = profile.getWallet();

        School school = profile.getUser().getSchool();
        if (school == null || school.getReferralAmountPerPoint() == null) {
            throw new BadRequestException("School not configured for point redemption");
        }

        UserPoints userPoints = userPointsRepository.findByUser(profile)
                .orElseThrow(() -> new BadRequestException("No points available"));

        if (userPoints.getPoints() < request.getPointsToRedeem()) {
            throw new BadRequestException("Insufficient points");
        }

        // Calculate redemption amount
        BigDecimal redemptionAmount = school.getReferralAmountPerPoint()
                .multiply(BigDecimal.valueOf(request.getPointsToRedeem()));

        // Create redemption record
        Redemption redemption = Redemption.builder()
                .user(profile)
                .school(profile.getUser().getSchool())
                .pointsRedeemed(request.getPointsToRedeem())
                .amount(redemptionAmount)
                .redemptionDate(LocalDateTime.now())
                .status(RedemptionStatus.COMPLETED)
                .build();
        redemptionRepository.save(redemption);

        // Update user points
        userPoints.setPoints(userPoints.getPoints() - request.getPointsToRedeem());
        userPointsRepository.save(userPoints);

        //  Process payment to user's account
        wallet.credit(redemptionAmount);
        walletRepository.save(wallet);


        return RedeemResponse.builder()
                .success(true)
                .amountRedeemed(redemptionAmount)
                .remainingPoints(userPoints.getPoints())
                .message("Points redeemed successfully")
                .build();
    }

    public UserPointsResponse getUserPoints(Long userId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile profile = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login"));


        UserPoints userPoints = userPointsRepository.findByUser(profile)
                .orElseGet(() -> UserPoints.builder()
                        .user(profile)
                        .points(0)
                        .build());

        School school = profile.getUser().getSchool();
        if (school == null) {
            throw new CustomNotFoundException("User is not associated with a school");
        }

        return UserPointsResponse.builder()
                .points(userPoints.getPoints())
                .schoolName(profile.getUser().getSchool().getSchoolName())
                .amountPerPoint(profile.getUser().getSchool().getReferralAmountPerPoint())
                .build();
    }
}
