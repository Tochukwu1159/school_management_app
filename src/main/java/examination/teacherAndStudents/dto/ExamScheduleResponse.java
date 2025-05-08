package examination.teacherAndStudents.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ExamScheduleResponse {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private Long classBlockId;
    private String classBlockName;
    private Long termId;
    private String termName;
    private Long yearId;
    private String yearName;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
}