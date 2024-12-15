package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class TransactionRequest {
    @Pattern(regexp = "[+-]?[0-9][0-9]*")
    private BigDecimal Amount;
    @Pattern(regexp = "[+-]?[0-9][0-9]*")
    private Long studentId;
    @Pattern(regexp = "[+-]?[0-9][0-9]*")
    private Long teacherId;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
