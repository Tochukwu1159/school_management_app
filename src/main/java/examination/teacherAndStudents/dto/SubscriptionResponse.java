package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.SubscriptionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        SubscriptionType subscriptionType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal amountPaid,
        String paymentReference
) {}