package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StudentTermRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long academicSessionId;
}