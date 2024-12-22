package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class SickLeaveCancelRequest {
    private Long sickLeaveId;
    private String cancelReason;
}
