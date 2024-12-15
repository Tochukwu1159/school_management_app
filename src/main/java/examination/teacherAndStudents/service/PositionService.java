package examination.teacherAndStudents.service;

import com.itextpdf.text.DocumentException;
import examination.teacherAndStudents.utils.StudentTerm;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface PositionService {
//    void generateAndSaveRanks();
    void updateAllPositionsForAClass(Long classLevelId, Long sessionId, StudentTerm term);

    void generateResultSummaryPdf(Long studentId, Long classLevelId, Long sessionId,StudentTerm term) throws IOException, DocumentException;
}
