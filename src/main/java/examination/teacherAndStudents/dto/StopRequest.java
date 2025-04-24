package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    private String stopName;

    private String address;

    @NotNull(message = "Sequence order is required")
    @Positive(message = "Sequence order must be positive")
    private int sequenceOrder;

    @NotNull(message = "Arrival time is required")
    private LocalTime arrivalTime;
}