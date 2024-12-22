package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SickLeaveRequest {
    private Long sessionId;
    private Long termId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
