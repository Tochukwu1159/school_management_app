package examination.teacherAndStudents.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedeemResponse {
    private boolean success;
    private String message;
    private BigDecimal amountRedeemed;
    private Integer remainingPoints;
}