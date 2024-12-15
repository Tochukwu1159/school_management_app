package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExaminationRequest {
    public int unitScore;
    private Long classLevelId;
    private Long subjectId;
    private String heading;
    private String examClass;


    private LocalDateTime examStartTime;
    private LocalDateTime examEndTime ;
}
