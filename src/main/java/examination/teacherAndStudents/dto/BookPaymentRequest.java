package examination.teacherAndStudents.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookPaymentRequest {
    private List<Long> bookIds;
    private Long studentId;
    private Long academicYearId;
    private Long termId;

    // Getters and Setters
}

