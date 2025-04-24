package examination.teacherAndStudents.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GradeRatingRequest {
    private int minMarks;
    private int maxMarks;
    private String grade;
    private String rating;

}
