package examination.teacherAndStudents.dto;

;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class PromotionCriteriaResponse {
    private Long id;
    private Long classBlockId;
    private String classBlockName;
    private Long currentSessionId;
    private String currentSessionName;
    private Long futureSessionId;
    private String futureSessionName;
    private Long promotedClassId;
    private String promotedClassName;
    private Long demotedClassId;
    private String demotedClassName;
    private int cutOffScore;
    private LocalDateTime createdAt;
}