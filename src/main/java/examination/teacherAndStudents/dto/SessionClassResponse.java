package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class SessionClassResponse {

    private Long id;
    private Long sessionId;
    private Long classBlockId;
    private Set<Long> profileIds;
    private int numberOfProfiles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}