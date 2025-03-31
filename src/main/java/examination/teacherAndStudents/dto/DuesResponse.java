package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuesResponse {
    private Long id;
    private String purpose;
    private BigDecimal amount;
    private Long studentTermId;
    private String studentTermName;
    private Long academicYearId;
    private String academicYearName;
}