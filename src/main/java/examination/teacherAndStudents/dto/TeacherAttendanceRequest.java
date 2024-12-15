package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.AttendanceStatus;
import examination.teacherAndStudents.utils.StudentTerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeacherAttendanceRequest {
    private Long teacherId;
    private LocalDateTime attendanceDate;
    private AttendanceStatus status;
    private StudentTerm term;

    // Constructors, getters, and setters
}
