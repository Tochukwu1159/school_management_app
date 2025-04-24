package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class GradeRatingResponse {
    private Long schoolId;
    private int minMarks;
    private int maxMarks;
    private String grade;
    private String rating;
}


