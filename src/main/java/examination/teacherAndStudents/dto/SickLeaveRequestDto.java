package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class SickLeaveRequestDto {
    private String action; // "approve" or "reject"
    private String reason; // Optional field for rejection reason
}