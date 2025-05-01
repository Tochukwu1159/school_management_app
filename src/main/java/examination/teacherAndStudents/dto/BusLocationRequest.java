package examination.teacherAndStudents.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusLocationRequest {
    private Long busId;
    private Double latitude;
    private Double longitude;
}