package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StopRequest {
    @NotBlank(message = "Stop name is required")
    @Size(max = 100, message = "Stop name must be less than 100 characters")
    private String stopName;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @NotNull(message = "Sequence order is required")
    @Positive(message = "Sequence order must be positive")
    private Integer sequenceOrder;

    @NotNull(message = "Arrival time is required")
    private LocalTime arrivalTime;
}