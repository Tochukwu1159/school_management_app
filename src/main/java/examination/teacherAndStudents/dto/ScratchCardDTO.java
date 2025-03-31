package examination.teacherAndStudents.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScratchCardDTO {
    private String cardNumber;
    private int currentUsageCount;
    private boolean isActive;
    private Long schoolId;
    private LocalDateTime createdAt;
    private BigDecimal price;
    private Long sessionId;
    private String sessionName;
    private Long termId;
    private String termName;
    private String pin;
    private Integer maxUsageCount;
    private LocalDateTime expiryDate;

}