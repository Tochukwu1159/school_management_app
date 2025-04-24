package examination.teacherAndStudents.dto;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.utils.PaymentMethod;
import examination.teacherAndStudents.utils.VisitorStatus;
import examination.teacherAndStudents.utils.VisitorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletFundingRequest {
    @NotBlank
    private String email;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency;

    private String callbackUrl;

    @NotNull
    private PaymentMethod paymentMethod;

    private Map<String, Object> metadata;

    // Additional fields that might be needed for some providers
    private String mobileNumber;
    private String firstName;
    private String lastName;

    public void validate() {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomInternalServerException("Amount must be positive");
        }
        // Add other validations as needed
    }
}