package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormTeacherAssignmentRequest {

    private Long sessionId;
    private Long classLevelId;

    @NotNull(message = "Assignments cannot be null")
    private List<Assignment> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Assignment {
        @NotNull(message = "Class block  ID cannot be null")
        private Long classBlockId;;
        @NotNull(message = "Teacher ID cannot be null")
        private Long teacherId;
    }
}