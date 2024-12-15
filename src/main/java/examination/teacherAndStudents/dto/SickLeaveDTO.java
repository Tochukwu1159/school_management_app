package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.SickLeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
@Data
@AllArgsConstructor
public class SickLeaveDTO {    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private SickLeaveStatus status; // Pending, Approved, Rejected

    public SickLeaveDTO() {

    }
//    private Long teacherId;


}
