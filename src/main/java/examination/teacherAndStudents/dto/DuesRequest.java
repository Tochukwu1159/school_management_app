package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class DuesRequest {
    private String purpose;
    private Long studentTerm;
    private Long academicYear;
    private BigDecimal amount;
}
