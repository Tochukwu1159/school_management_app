package examination.teacherAndStudents.dto;


import lombok.Data;

import java.util.List;

@Data
public class GradeRatingResponseArray {
    private List<GradeRatingResponse> gradeRatingResponses;
}