package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.DayOfWeek;
import examination.teacherAndStudents.utils.StudentTerm;
import examination.teacherAndStudents.utils.TimetableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.Year;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimetableCreationRequest {
    private Long term;
    private Long yearId;
    private Long classBlockId;
    private DayOfWeek dayOfWeek;
    private TimetableType timetableType;
    private List<SubjectScheduleRequest> subjectSchedules;
}
