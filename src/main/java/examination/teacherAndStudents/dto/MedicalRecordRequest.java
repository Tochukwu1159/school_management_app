package examination.teacherAndStudents.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MedicalRecordRequest {
    private Long studentId;
    private LocalDateTime recordDate;
    private String details;
}
