package examination.teacherAndStudents.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class StaffPayrollRequest {

    @NotNull
    private double bonuses;

    @NotNull
    private double deductions;


    private String remarks;

    @NotNull
    private Long staffId; // ID of the staff member


}


