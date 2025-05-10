package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteRequest {
    @NotBlank(message = "Route name is required")
    @Size(max = 100, message = "Route name must be less than 100 characters")
    private String routeName;

    @NotBlank(message = "Start point is required")
    private String startPoint;

    @NotBlank(message = "End point is required")
    private String endPoint;

    // Constructors, getters, and setters
}