package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UserResponse {
    private  String responseCode;
    private  String responseMessage;
    private  AccountInfo accountInfo;

    public UserResponse(String responseCode, String responseMessage, AccountInfo accountInfo) {
       this.responseCode = responseCode;
       this.responseMessage = responseMessage;
       this.accountInfo = accountInfo;

    }
}

