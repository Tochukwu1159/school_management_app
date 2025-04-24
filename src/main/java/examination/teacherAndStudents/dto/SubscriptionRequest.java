package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.PaymentMethod;
import examination.teacherAndStudents.utils.SubscriptionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionRequest {
    private SubscriptionType subscriptionType;
    @NotNull
    PaymentMethod paymentMethod;
}
