package examination.teacherAndStudents.factory;

import examination.teacherAndStudents.templates.reportCard.ReportCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReportCardFactory {

    @Autowired
    private Map<String, ReportCard> reportCardMap; // Spring injects all implementations

    public ReportCard getReportCard(String schoolName) {
        return reportCardMap.get(schoolName + "ReportCard");
    }
}
