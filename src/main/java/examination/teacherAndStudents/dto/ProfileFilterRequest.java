package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class ProfileFilterRequest {
    private String role;
    private String status;
    private int page; // Page number
    private int size; // Page size
}
