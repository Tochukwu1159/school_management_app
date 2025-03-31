package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}