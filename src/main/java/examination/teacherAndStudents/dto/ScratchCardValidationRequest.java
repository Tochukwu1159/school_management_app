package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScratchCardValidationRequest {
    @NotBlank
    private String cardNumber;

    @NotBlank
    @Size(min = 6, max = 6)
    private String pin;

}