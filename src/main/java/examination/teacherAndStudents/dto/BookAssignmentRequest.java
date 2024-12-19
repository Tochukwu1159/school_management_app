package examination.teacherAndStudents.dto;

import lombok.Data;

import java.util.List;
@Data
public class BookAssignmentRequest {
    private List<Long> bookIds;
    private Long studentId;
    private Long session;
    private double amountPaid;
}
