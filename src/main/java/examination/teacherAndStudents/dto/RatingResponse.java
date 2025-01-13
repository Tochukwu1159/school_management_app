package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class RatingResponse {
    private Long id;
    private Long schoolId;
    private int minMarks;
    private int maxMarks;
    private String rating;

    // Getters and Setters
}
