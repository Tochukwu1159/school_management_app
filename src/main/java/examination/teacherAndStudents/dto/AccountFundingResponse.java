package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class AccountFundingResponse {
    private String paymentUrl;
    private String reference;

    public AccountFundingResponse(String paymentUrl, String reference) {
        this.paymentUrl = paymentUrl;
        this.reference = reference;
    }
}
