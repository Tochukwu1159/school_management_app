package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class PaymentProviderRequest {
    private String provider;
    private String callbackUrl;
}
