package examination.teacherAndStudents.service;

import com.itextpdf.text.DocumentException;
import examination.teacherAndStudents.utils.StudentTerm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;

public interface PositionService {
//    void generateAndSaveRanks();
    void updatePositionsForClass(Long classBlockId, Long sessionId, Long termId);

    void generateResultSummaryPdf(Long studentId, Long classBlockId, Long sessionId,Long term) throws IOException, DocumentException;

    void updatePositionForSessionClassForJob(Long classBlockId, Long sessionId, Long termId);

    void generateReportCardSummaryJob(Long sessionId);
}
