package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaintenanceRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount spent is required")
    private Double amountSpent;

    @NotNull(message = "Transport ID is required")
    private Long transportId;

    private Long maintainedBy;
}
