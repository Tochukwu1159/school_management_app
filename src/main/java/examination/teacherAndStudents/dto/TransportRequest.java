package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportRequest {
    private Long busRouteId;
    private String vehicleName;
    private String vehicleNumber;
    private Long driverId;
    private String licenceNumber;
    private String phoneNumber;
    private int capacity;
    // getters and setters
}
