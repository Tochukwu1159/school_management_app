package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class DuesRequest {
    private Long studentId;
    private String purpose;
    private StudentTerm term;
    private BigDecimal amount;
}
