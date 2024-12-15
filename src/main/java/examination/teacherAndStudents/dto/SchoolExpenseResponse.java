package examination.teacherAndStudents.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SchoolExpenseResponse {
    private Long id;
    private BigDecimal amount;
    private String purpose;
    private String type;
}
