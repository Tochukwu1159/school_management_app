package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDetails {
    private String recipient;          // Email address of the recipient
    private String subject;            // Email subject
    private String templateName;       // Name of the Thymeleaf HTML template
    private Map<String, Object> model;
    private String attachmentPath;         // File path for attachment

    // Getter and setter for the attachment field
}
