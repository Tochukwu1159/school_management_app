package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StudentPromotionRequest;

public interface PromotionService {
    void promoteStudents(StudentPromotionRequest request);
}
