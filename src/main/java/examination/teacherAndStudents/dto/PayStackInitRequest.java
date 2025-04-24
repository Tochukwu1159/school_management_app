package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class PayStackInitRequest {
    private String email;
    private BigDecimal amount; // in kobo
    private String reference;
    private String callbackUrl;
    private Map<String, Object> metadata;
    private String currency;
    private String channels; // e.g., "card, bank, ussd"

    public static PayStackInitRequest fromPaymentRequest(WalletFundingRequest request) {
        return PayStackInitRequest.builder()
                .email(request.getEmail())
                .amount(request.getAmount().multiply(BigDecimal.valueOf(100))) // Convert to kobo
                .callbackUrl(request.getCallbackUrl())
                .metadata(request.getMetadata())
                .currency(request.getCurrency())
                .build();
    }
}
