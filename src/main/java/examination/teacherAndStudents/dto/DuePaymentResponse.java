package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuePaymentResponse {
    private Long id;
    private Long dueId;
    private String duePurpose;
    private Long profileId;
    private String profileName;
    private Long studentTermId;
    private String studentTermName;
    private Long academicYearId;
    private String academicYearName;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}