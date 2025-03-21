package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.Result;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultSummary {
    private Long studentId;
    private Long classLevelId;
    private Long sessionId;
    private Long term;
    private  Map<String, Map<String, Object>>  scores; // Subject -> Score
    private List<Result> results; // List of Result objects (from the result table)
    private double averageScore; // From the position table
    private int positionRank;

    // Constructor, getters, and setters
}