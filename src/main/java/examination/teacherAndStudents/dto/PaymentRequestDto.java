package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
@Data
@AllArgsConstructor
public class PaymentRequestDto {

   private String email;
    private  BigDecimal amount;
    private  String callbackUrl;
    private Map<String, Object> metadata;
}