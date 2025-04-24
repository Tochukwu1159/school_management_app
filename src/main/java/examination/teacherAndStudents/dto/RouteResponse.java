package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.dto.StopResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponse {
    private Long id;
    private String routeName;
    private String startPoint;
    private String endPoint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StopResponse> stops;
}