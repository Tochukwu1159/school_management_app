package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.VisitorType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorsRequest {
    @NotBlank(message = "Purpose is required")
    @Size(max = 255, message = "Purpose must not exceed 255 characters")
    private String purpose;

    @NotBlank(message = "Visitor's Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Host name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String hostName;

    @NotNull(message = "Visitor type is required")
    private VisitorType visitorType;


    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

}
