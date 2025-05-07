package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

// Data class to hold the statistics report
@Getter
@Data
@AllArgsConstructor
public class StatisticsReport {
    private final String academicSession;
    private final String classBlock;
    private final Map<String, Long> scoreDistribution;
    private final double meanScore;
    private final double medianScore;
    private final double standardDeviation;
    private final int totalStudents;


@Data
@AllArgsConstructor
    public static class ScoreRange {
        private final double min;
        private final double max;
        private final String label;
    }
}