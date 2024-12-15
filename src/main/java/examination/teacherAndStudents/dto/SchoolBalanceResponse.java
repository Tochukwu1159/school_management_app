package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchoolBalanceResponse {
    private BigDecimal TotalMoneyFunded;
    private BigDecimal balance;
    private BigDecimal totalStudentMoneySent;
}
