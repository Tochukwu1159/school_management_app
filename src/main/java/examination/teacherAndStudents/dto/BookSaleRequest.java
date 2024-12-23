package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookSaleRequest {
    private String title;
    private String author;
    private String idNo;
    private double price;
    private Long classId;
    private Long subjectId;
    private int numberOfCopies;
}
