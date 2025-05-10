package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.DisciplinaryActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DisciplinaryActionRequest {
    @NotNull(message = "Profile ID is required")
    private Long profileId;

    @NotNull(message = "Issued by profile ID is required")
    private Long issuedById;

    @NotNull(message = "Action type is required")
    private DisciplinaryActionType actionType;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
}