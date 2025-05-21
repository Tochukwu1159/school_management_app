package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ResultService {
    Result calculateResult(Long classLevelId, Long studentId, String subjectName, Long sessionId, Long termId) ;
   void calculateAverageResult(Long sessionId, Long classLevelId, Long termId);
    void promoteStudents(Long sessionId, Long presentClassId, Long futureSessionId, Long futurePClassId, Long futureFClassId, int cutOff);
    void updateSessionAverage(Set<Profile> studentProfiles, SessionClass sessionClass);
    List<SessionAverage> getTop5StudentsPerClass(Long classBlock, Long academicYear);

    Map<ClassBlock, List<SessionAverage>> getTop5StudentsForAllClasses(Long academicYearId);
    void updateSessionAverageForJob(AcademicSession academicYear);
    void calculateAverageResultJob(Long sessionId, Long termId);

}