package examination.teacherAndStudents.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class TransferRequest {
    private String registrationNumber;

    BigDecimal amount;
}
