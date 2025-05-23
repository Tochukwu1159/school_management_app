package examination.teacherAndStudents.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ExamScheduleRequest {
    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private Long classBlockId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Exam date is required")
//    @PastOrPresent(message = "Exam date cannot be in the future")
    private LocalDate examDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}
