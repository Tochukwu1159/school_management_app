package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;
@Data
public class AttendanceDto {
    private LocalDate date;
    private AttendanceStatus status;
}
