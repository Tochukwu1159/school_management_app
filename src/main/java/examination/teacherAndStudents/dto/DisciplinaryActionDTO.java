package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.DisciplinaryActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DisciplinaryActionDTO {
    @NotNull
    private String regNo;

    @NotNull
    private DisciplinaryActionType actionType;

    @NotBlank
    private String reason;

    private String description;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private boolean active = true;
}