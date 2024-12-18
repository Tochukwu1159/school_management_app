package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class AcademicSessionResponse {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    public AcademicSessionResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}