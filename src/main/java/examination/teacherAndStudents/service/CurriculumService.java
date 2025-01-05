package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.CurriculumRequest;
import examination.teacherAndStudents.dto.CurriculumResponse;

public interface CurriculumService {
    CurriculumResponse addCurriculumToClassSubject(Long classSubjectId, CurriculumRequest request);
}
