package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookSaleResponse {
    private Long id;
    private String title;
    private String author;
    private String idNo;
    private double price;
    private int numberOfCopies;
    private String classLevelName;
    private String subjectName;
}
