package examination.teacherAndStudents.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FeeResponseDto {
    private Long id;
    private String categoryName;
    private String description;
    private BigDecimal amount;
    private boolean isCompulsory;
    private Long schoolId;
    private Long sessionId;
    private Long classLevelId;
    private Long subClassId;
    private Long termId;
    private boolean active;
    private boolean archived;
}