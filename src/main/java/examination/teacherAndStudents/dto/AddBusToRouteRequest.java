package examination.teacherAndStudents.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddBusToRouteRequest {
    private String vehicleNumber;
    private String vehicleName;
    private String licenceNumber;
    private Long driverId;
    private Integer capacity;
    private Long routeId;
}