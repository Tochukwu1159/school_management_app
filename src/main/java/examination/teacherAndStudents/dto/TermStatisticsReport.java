package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
@Data
@AllArgsConstructor
public  class TermStatisticsReport {
    private final String academicSession;
    private final String classBlock;
    private final String term;
    private final Map<String, Long> scoreDistribution;
    private final double meanScore;
    private final double medianScore;
    private final double standardDeviation;
    private final int totalStudents;}
