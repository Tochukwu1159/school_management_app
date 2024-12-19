package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.*;

import java.util.List;

public interface ResultService {
    Result calculateResult(Long classLevelId, Long studentId, String subjectName, Long sessionId, Long termId) ;
   void calculateAverageResult(Long sessionId, Long classLevelId, Long termId);
    void promoteStudents(Long sessionId, Long presentClassId, Long futureSessionId, Long futurePClassId, Long futureFClassId, int cutOff);
    void updateSessionAverage(List<Profile> studentProfiles, ClassBlock classBlock, AcademicSession academicYear);

}