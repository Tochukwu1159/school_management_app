package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ApplicationResponse {

    private ApplicationStatus applicationStatus;
    private String reviewMessage;
    private LocalDate examDate;  // if applicable
    private Integer examScore;   // if applicable
    private Boolean passedExam;
}
