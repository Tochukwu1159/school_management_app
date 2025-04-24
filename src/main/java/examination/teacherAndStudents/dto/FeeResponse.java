package examination.teacherAndStudents.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class FeeResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal amount;
    private boolean isCompulsory;

    private Long schoolId;
    private Long sessionId;
    private Long classLevelId;
    private Long subClassId;
    private Long termId;

    // Getters and setters
}