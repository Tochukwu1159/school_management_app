package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StopResponse {
    private Long stopId;
    private String stopName;
    private String address;
    private int sequenceOrder;
    private LocalTime arrivalTime;
    private Double latitude;
    private Double longitude;
}