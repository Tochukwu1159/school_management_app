package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScratchCardValidationResponse {
    @NotBlank
    private String cardNumber;
    @NotBlank
    @Size(min = 4, max = 4)
    private String pin;
    private int remainingUses;

    private LocalDateTime validUntil;

}


// Your existing ResultDTO
