package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.GradeRequest;
import examination.teacherAndStudents.dto.GradeResponse;
import examination.teacherAndStudents.entity.Grade;
import examination.teacherAndStudents.entity.School;

import java.util.List;

public interface GradeService {
    GradeResponse createGrade(GradeRequest gradeRequest);
    GradeResponse updateGrade(Long gradeId, GradeRequest gradeRequest);
    void deleteGradeById(Long gradeId);
    GradeResponse getGradeById(Long gradeId);
    List<GradeResponse> findAllGradesBySchool(Long schoolId);

//    Grade calculateGrade(School school, double totalMarks);
}
