package examination.teacherAndStudents.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class PayStackErrorResponse {
    private boolean status;
    private String message;
    private List<String> errors;

    // Getters and setters...
}