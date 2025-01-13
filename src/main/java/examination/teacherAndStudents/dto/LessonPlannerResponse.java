package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.LessonStatus;
import examination.teacherAndStudents.utils.TeachingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class LessonPlannerResponse {
    private Long id;
    private String week;
    private String day;
    private String subject;
    private LessonStatus status;
    private TeachingStatus teachingStatus;
    private String lessonTopic;
    private String period;
    private String updatedTimeAfterTeaching;
    private String teacherName;
    private String schoolName;
    private String termName;
    private String classBlockName;
}

