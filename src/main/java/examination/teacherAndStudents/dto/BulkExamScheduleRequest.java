
package examination.teacherAndStudents.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkExamScheduleRequest {
    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotNull(message = "Academic session ID is required")
    private Long yearId;

    @NotNull(message = "Class block ID is required")
    private Long classBlockId;

    @NotNull(message = "Subject schedules are required")
    @Valid
    private List<ExamScheduleRequest> subjectSchedules;
}
