package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Old Password is required")
    private String oldPassword;
    @NotBlank(message = "New Password is required")
    private  String newPassword;
    @NotBlank(message = "Confirm Password is required")
    private  String confirmPassword;

}
