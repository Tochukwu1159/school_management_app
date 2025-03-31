package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponses {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentRegistrationNumber; // Added student unique identifier
    private Long classBlockId;
    private String classBlockName;
    private Long classLevelId; // Added class level information
    private String classLevelName;
    private Long academicYearId;
    private String academicYearName;
    private Long studentTermId;
    private String studentTermName;
    private LocalDateTime date;
    private AttendanceStatus status;
    private String remarks; // Optional field for any notes
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long daysPresent;
    private Long daysAbsent;
    private
    double percentageAttendance;

    // Additional useful fields that might be needed in the response
    private String subjectName; // If attendance is for a specific subject
    private String teacherName; // Name of the teacher who marked attendance
    private boolean excused; // Flag for excused absences
    private Map<LocalDate, DailyAttendance> dailyAttendance;

    @Data
    @Builder
    public static class DailyAttendance {
        private LocalDateTime dateTime;
        private AttendanceStatus status;
        private String remarks; // Optional field if you want to add notes
    }
}