package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
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
public class UserFormData {
    private String eventRef;
    private String image;
    private String title;
    @NotNull(message = "Event start time is required")
    private LocalDateTime eventStartDateTime;
    @NotNull(message = "Event end time is required")
    private LocalDateTime eventEndDateTime;

    private String description;
    private String liveStreamLink;
    private List<String> designationOfMinistry;

    // Getters and setters
}