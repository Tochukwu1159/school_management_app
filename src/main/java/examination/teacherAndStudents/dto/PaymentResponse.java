package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long studentId;
    private Long studentFeeId;
    private String feeName;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod method;
    private String reference;
    private BigDecimal balance;
    private String authorizationUrl;


}

