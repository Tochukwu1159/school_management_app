package examination.teacherAndStudents.service;

import com.itextpdf.text.DocumentException;
import examination.teacherAndStudents.utils.StudentTerm;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface PositionService {
//    void generateAndSaveRanks();
    void updatePositionsForClass(Long classBlockId, Long sessionId, Long termId);

    void generateResultSummaryPdf(Long studentId, Long classLevelId, Long sessionId,Long term) throws IOException, DocumentException;
}
