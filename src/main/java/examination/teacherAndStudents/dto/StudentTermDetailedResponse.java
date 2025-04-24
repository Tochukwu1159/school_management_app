package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.TermStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class StudentTermDetailedResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate resultReadyDate;
    private AcademicSessionResponse academicSession;
    private TermStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}