package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record HomeworkRequest(
        @NotNull(message = "Subject ID is required")
        Long subjectId,

        @NotNull(message = "Academic session ID is required")
        Long sessionId,

        @NotNull(message = "Class block ID is required")
        Long classId,

        @NotNull(message = "Term ID is required")
        Long termId,

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
        String title,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Pattern(regexp = "^(https?://)?([\\w-\\.]+\\.[a-zA-Z]{2,6})(/.*)?$|^$",
                message = "Invalid URL format")
        String fileUrl,

        int mark,

        @NotNull(message = "Submission date is required")
        @Future(message = "Submission date must be in the future")
        LocalDateTime submissionDate
) {}