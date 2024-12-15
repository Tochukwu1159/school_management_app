package examination.teacherAndStudents.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SchoolExpenseRequest {
    private BigDecimal amount;
    private String purpose;
    private String type;
    // Other fields, getters, setters...
}
