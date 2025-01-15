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
public class StaffMovementRequest {
    private Long staffId;
    private String purpose;
    private String approvedBy;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
}