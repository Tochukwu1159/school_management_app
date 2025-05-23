package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class SessionClassRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Class Block ID is required")
    private Long classBlockId;

    @NotNull(message = "Profile IDs are required")
    private Set<Long> profileIds;
}