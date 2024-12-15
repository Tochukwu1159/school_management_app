package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.Result;
import examination.teacherAndStudents.utils.StudentTerm;

public interface ResultService {
    Result calculateResult(Long classLevelId, Long studentId, String subjectName, Long sessionId, StudentTerm term) ;
   void calculateAverageResult(Long userId, Long classLevelId, Long sessionId, StudentTerm term);

}