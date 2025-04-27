package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class AccountFundingRequest {
    private String studentId;
    private String email;
    private String amount;
    private String gateway;
}
