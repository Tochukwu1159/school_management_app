package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BulkAttendanceRequest {

    @NotNull(message = "Date cannot be null")
    @FutureOrPresent(message = "Date cannot be in the past")
    private LocalDateTime date;

    @NotNull(message = "Student term ID cannot be null")
    @Positive(message = "Student term ID must be a positive number")
    private Long studentTermId;

    @NotNull(message = "Class block ID cannot be null")
    @Positive(message = "Class block ID must be a positive number")
    private Long classBlockId;

    @NotEmpty(message = "Student attendances list cannot be empty")
    @Valid
    private List<StudentAttendance> studentAttendances;

    @Data
    public static class StudentAttendance {

        @NotNull(message = "Student ID cannot be null")
        @Positive(message = "Student ID must be a positive number")
        private Long studentId;

        @NotNull(message = "Attendance status cannot be null")
        private AttendanceStatus status;
    }
}