package examination.teacherAndStudents.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ApplicationReviewDto {
    private boolean approved;
    private int examScore;
    private boolean passed;
    private boolean rejected;
    private boolean incomplete;
    private LocalDateTime examDate;
    private Set<String> missingDocuments; // If additional documents needed
    private String rejectionReason; // If rejected
    // getters & setters
}