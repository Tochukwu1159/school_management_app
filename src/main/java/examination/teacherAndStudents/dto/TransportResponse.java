package examination.teacherAndStudents.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportResponse {
    private Long id;
    private String routeName;
    private String vehicleNumber;
    private String vehicleName;
    private String licenceNumber;
    private Long busRouteId;

    // Getters and Setters
}
