package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.SubscriptionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionRequest {
    private SubscriptionType subscriptionType;
    private int amount;
}
