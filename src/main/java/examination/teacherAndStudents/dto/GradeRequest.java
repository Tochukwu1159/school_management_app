package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class GradeRequest {
    private Long schoolId;
    private int minMarks;
    private int maxMarks;
    private String grade;

    // Getters and Setters
}
