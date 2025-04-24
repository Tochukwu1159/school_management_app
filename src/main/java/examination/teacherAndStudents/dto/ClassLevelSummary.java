package examination.teacherAndStudents.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassLevelSummary {
    private Long classLevelId;
    private String className;
    private BigDecimal totalAmountExpected;
    private BigDecimal totalAmountPaid;
    // ... other fields
}