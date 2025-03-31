package examination.teacherAndStudents.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PayStackTransactionRequest {

        @NotNull(message = "Amount cannot be null")
        @Digits(integer = 12, fraction = 2, message = "Amount must have up to 12 digits and 2 decimal places")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        private String email;

        @Size(max = 100, message = "Reference cannot exceed 100 characters")
        private String reference;

        @Size(max = 100, message = "Callback URL cannot exceed 100 characters")
        private String callbackUrl;

        @Size(max = 50, message = "Currency cannot exceed 50 characters")
        @Builder.Default
        private String currency = "NGN";

        private Map<String, String> metadata;

        @Size(max = 100, message = "Transaction charge cannot exceed 100 characters")
        private String transactionCharge;

        @Builder.Default
        private boolean bearer = true; // Default to customer bearing charges

        // Custom validation method can be added here if needed
        public boolean isAmountValid() {
                return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
        }
}