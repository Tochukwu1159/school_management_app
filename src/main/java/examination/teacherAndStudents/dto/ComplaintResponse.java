package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ComplaintResponse {
    private Long id;

    private String feedbackText;
    private LocalDateTime submittedTime;
    private String replyText;
    private LocalDateTime replyTime;
    private String reply;
    private UserResponse user;
    // Other fields...

    // Constructors, getters, and setters...
}
