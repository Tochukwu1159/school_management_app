package examination.teacherAndStudents.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ApplicationReviewDto {
    private boolean approved;
    private boolean rejected;
    private boolean incomplete;
    private int examScore;
    private boolean passed;
    private LocalDateTime examDate;
    private Set<String> missingDocuments;
    private String rejectionReason;
}