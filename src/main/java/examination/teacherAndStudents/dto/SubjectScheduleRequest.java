package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubjectScheduleRequest {
    private Long subjectId;
    private String topic;
    private Long teacherId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
