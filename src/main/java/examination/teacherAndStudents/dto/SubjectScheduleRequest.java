package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubjectScheduleRequest {
    private Long subjectId;
    private Long teacherId;
    private boolean breakTime;
    private String startTime; // Changed to String to handle both formats
    private String endTime;   // Changed to String to handle both formats
}