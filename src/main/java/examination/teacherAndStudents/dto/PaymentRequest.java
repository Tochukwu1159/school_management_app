package examination.teacherAndStudents.dto;
import java.math.BigDecimal;
import java.time.LocalDate;

import examination.teacherAndStudents.utils.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class PaymentRequest {


    private Long feeId;

    private BigDecimal amount;

    private PaymentMethod method;

    private String reference;

    private String notes;


}
