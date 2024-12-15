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
    private String driverName;
    private String driverAddress;
    private String licenceNumber;
    private String phoneNumber;

    // Getters and Setters
}
