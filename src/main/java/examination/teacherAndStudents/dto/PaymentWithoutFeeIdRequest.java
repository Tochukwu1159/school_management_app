
package examination.teacherAndStudents.dto;
import java.math.BigDecimal;
import java.time.LocalDate;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.utils.PaymentMethod;
import examination.teacherAndStudents.utils.Purpose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentWithoutFeeIdRequest {


    private BigDecimal amount;

    private PaymentMethod method;

    private String description;

    private Purpose purpose;

    private String reference;

    private String notes;


}
