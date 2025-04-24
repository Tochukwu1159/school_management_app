package examination.teacherAndStudents.dto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
// UserPointsResponse.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPointsResponse {
    private Integer points;
    private String schoolName;
    private BigDecimal amountPerPoint;
}
