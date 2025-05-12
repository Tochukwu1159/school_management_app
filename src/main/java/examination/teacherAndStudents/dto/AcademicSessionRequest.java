package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AcademicSessionRequest {

    @NotBlank(message = "Name is required")
    private Long sessionNameId;

    @NotNull(message = "Academic Start date is required")
    private LocalDate startDate;

    private LocalDate resultReadyDate;

    @NotNull(message = "Academic End date is required")
    private LocalDate endDate;

    private LocalDate firstTermStartDate;
    private LocalDate firstTermEndDate;
    private LocalDate firstTermResultReadyDate;

    private LocalDate secondTermStartDate;
    private LocalDate secondTermEndDate;
    private LocalDate secondTermResultReadyDate;

    private LocalDate thirdTermStartDate;
    private LocalDate thirdTermEndDate;
    private LocalDate thirdTermResultReadyDate;
}

