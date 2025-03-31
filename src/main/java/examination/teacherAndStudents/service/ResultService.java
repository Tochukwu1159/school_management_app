package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.*;

import java.util.List;
import java.util.Map;

public interface ResultService {
    Result calculateResult(Long classLevelId, Long studentId, String subjectName, Long sessionId, Long termId) ;
   void calculateAverageResult(Long sessionId, Long classLevelId, Long termId);
    void promoteStudents(Long sessionId, Long presentClassId, Long futureSessionId, Long futurePClassId, Long futureFClassId, int cutOff);
    void updateSessionAverage(List<Profile> studentProfiles, ClassBlock classBlock, AcademicSession academicYear);
    List<SessionAverage> getTop5StudentsPerClass(ClassBlock classBlock, AcademicSession academicYear);

    Map<ClassBlock, List<SessionAverage>> getTop5StudentsForAllClasses(AcademicSession academicYear);
    void updateSessionAverageForJob(AcademicSession academicYear);
    void calculateAverageResultJob(Long sessionId, Long termId);

}