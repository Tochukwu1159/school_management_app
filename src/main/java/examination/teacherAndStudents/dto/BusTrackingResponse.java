package examination.teacherAndStudents.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusTrackingResponse {
    private Long busId;
    private Double busLatitude;
    private Double busLongitude;
    private String stopName;
    private Double stopLatitude;
    private Double stopLongitude;
    private Double distance; // in kilometers
    private String estimatedTime; // in minutes
}