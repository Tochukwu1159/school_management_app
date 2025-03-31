package examination.teacherAndStudents.dto;
import examination.teacherAndStudents.utils.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkSubmissionResponse {
    private Long id;
    private Long homeworkId;
    private String homeworkTitle;
    private Long studentId;
    private String studentName;
    private String fileUrl;
    private LocalDateTime submittedAt;
    private Double obtainedMark;
    private SubmissionStatus status;
}