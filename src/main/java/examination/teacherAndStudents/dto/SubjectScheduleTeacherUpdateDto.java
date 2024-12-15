package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubjectScheduleTeacherUpdateDto {
    private DayOfWeek dayOfWeek;
    private Long scheduleId;
    private String topic;
}
