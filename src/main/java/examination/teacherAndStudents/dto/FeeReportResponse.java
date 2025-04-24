package examination.teacherAndStudents.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FeeReportResponse {
    private Long schoolId;
    private Long sessionId;
    private Long classLevelId;
    private BigDecimal totalAmountExpected;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalBalance;
    private int totalStudents;
    private int paidStudents;
    private int unpaidStudents;
    private List<ClassLevelSummary> classLevelSummaries;

    // Getters and setters
}