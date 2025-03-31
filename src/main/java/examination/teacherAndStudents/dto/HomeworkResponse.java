package examination.teacherAndStudents.dto;

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
public class HomeworkResponse {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private Long academicSessionId;
    private String academicSessionName;
    private Long classBlockId;
    private String classBlockName;
    private Long termId;
    private String termName;
    private Long teacherId;
    private String teacherName;
    private String title;
    private String description;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime submissionDate;
}