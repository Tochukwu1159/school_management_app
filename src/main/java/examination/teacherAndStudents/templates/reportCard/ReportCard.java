package examination.teacherAndStudents.templates.reportCard;

import examination.teacherAndStudents.dto.ResultSummary;

public interface ReportCard {
    String generateReport(ResultSummary result);
}
