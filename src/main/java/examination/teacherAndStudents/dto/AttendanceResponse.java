package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.utils.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponse {
    private String studentName;
    private Long studentId;
    private long daysPresent;
    private long daysAbsent;
    private double percentageAttendance;
    private Map<DayOfWeek, List<Attendance>> attendanceByDayOfWeek;
}


