package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiometricVerificationResult {
    private boolean verified;
    private double score;
    private String templateHash;
    // ... getters and setters ...
}
