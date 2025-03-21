package examination.teacherAndStudents.templateService;

import examination.teacherAndStudents.dto.ResultSummary;
import examination.teacherAndStudents.factory.ReportCardFactory;
import examination.teacherAndStudents.templates.reportCard.ReportCard;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportCardService {


    private final ReportCardFactory reportCardFactory;

    public String generateReportCard(String schoolName, ResultSummary result) {
        ReportCard reportCard = reportCardFactory.getReportCard(schoolName);
        if (reportCard == null) {
            throw new IllegalArgumentException("No report card template found for school: " + schoolName);
        }
        return reportCard.generateReport(result);
    }
}