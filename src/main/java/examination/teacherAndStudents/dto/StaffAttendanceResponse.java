package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StaffAttendanceResponse {
    private ProfileData staffProfile;
    private double attendancePercentage;
    private long daysPresent;
    private long daysAbsent;
    private long daysLate;
    private long totalDays;


}