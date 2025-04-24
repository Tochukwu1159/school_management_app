package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundWalletRequest {
    private String amount;
    private String provider; // paystack, flutterwave, stripe, etc.
    private String currency; // NGN, USD, etc. - optional with default
    private String callbackUrl; // optional - can be configured at provider level
    private String email; // optional - can be taken from authenticated user
    private Map<String, Object> metadata; // additional provider-specific data

    public BigDecimal getAmountAsBigDecimal() {
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        }
    }
}