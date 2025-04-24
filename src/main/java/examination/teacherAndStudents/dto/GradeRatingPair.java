package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GradeRatingPair {
    private String grade;
    private String rating;
}