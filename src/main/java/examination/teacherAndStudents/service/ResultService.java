package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.*;

public interface ResultService {
    Result calculateResult(Long classLevelId, Long studentId, String subjectName, Long sessionId, Long termId) ;
   void calculateAverageResult(Long userId, Long classLevelId, Long sessionId, Long termId);

}