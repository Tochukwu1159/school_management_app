package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DuePaymentResponse {
    private Long id;
    private Long dueId;
    private Long userId;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;

    // Getters and Setters duePayment.getId(),
}
