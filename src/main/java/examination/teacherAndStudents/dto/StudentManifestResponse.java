package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentManifestResponse {
    private Long id;
    private Long profileId;
    private String studentName;
    private String status;
    private String pickupPerson;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}