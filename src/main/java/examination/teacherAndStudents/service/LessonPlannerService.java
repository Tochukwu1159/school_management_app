package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.LessonPlannerRequest;
import examination.teacherAndStudents.dto.LessonPlannerResponse;

import java.util.List;

public interface LessonPlannerService {
    LessonPlannerResponse createLessonPlanner(LessonPlannerRequest request);
    LessonPlannerResponse updateLessonPlanner(Long id, LessonPlannerRequest request);
    void deleteLessonPlanner(Long id);
    LessonPlannerResponse getLessonPlannerById(Long id);
    List<LessonPlannerResponse> getAllLessonPlanners();
}