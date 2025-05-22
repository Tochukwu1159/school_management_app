package examination.teacherAndStudents.dto;


import examination.teacherAndStudents.utils.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class WalletResponse {
    private Long walletId;
    private BigDecimal balance;
    private BigDecimal totalMoneySent;
    private WalletStatus walletStatus;
}
