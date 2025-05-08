package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentPromotionRequest {
    @NotNull
    private List<PromotionData> promotion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionData {
        @NotNull
        private Long classBlockId;
        @NotNull
        private Long studentId;
        @NotNull
        private Long promotedClassBlock;
    }
}
