package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// RedeemPointsRequestDto.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedeemPointsRequestDto {
    @NotNull(message = "Points to redeem is required")
    @Min(value = 1, message = "Must redeem at least 1 point")
    private Integer pointsToRedeem;
}