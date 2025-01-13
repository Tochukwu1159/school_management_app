package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class GradeResponse {
    private Long id;
    private Long schoolId;
    private int minMarks;
    private int maxMarks;
    private String grade;

    // Getters and Setters
}
