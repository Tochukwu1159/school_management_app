package examination.teacherAndStudents.dto;

import lombok.Data;
import java.util.List;

@Data
public class GradeRatingRequestArray {
    private List<GradeRatingRequest> gradeRatingRequests;
}