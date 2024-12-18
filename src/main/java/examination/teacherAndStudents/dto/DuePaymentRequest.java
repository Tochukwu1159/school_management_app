package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.StudentTerm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DuePaymentRequest {
    private Long dueId;
    private Long sessionId;
    private Long term;

    // Getters and Setters
}
