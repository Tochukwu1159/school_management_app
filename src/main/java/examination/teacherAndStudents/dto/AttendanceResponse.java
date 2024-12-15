package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.Attendance;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Data
public class AttendanceResponse {
    private String studentName;
    private Long studentId;
    private long daysPresent;
    private long daysAbsent;
    private double percentageAttendance;
    private Map<DayOfWeek, List<Attendance>> attendanceByDayOfWeek;
}
