package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaintenanceResponse {

    private Long id;
    private String description;
    private Double amountSpent;
    private Long transportId;
    private String transportVehicleNumber;
    private String transportBusNumber;
    private String maintainedByName;
    private Long maintainedById;
    private LocalDateTime maintenanceDate;
    private LocalDateTime updatedAt;
}