package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

public record HomeworkSubmissionRequest(
        @NotNull Long homeworkId,
        @NotBlank String fileUrl,
        @NotNull @PastOrPresent LocalDateTime submittedAt
) {}