package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StaffMovementResponse {
    private Long id;
    private Long staffId;
    private String purpose;
    private String approvedBy;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}