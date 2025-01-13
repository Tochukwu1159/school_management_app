package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.CurriculumRequest;
import examination.teacherAndStudents.dto.CurriculumResponse;

import java.util.List;

public interface CurriculumService {
    // Add a new curriculum to a class subject
    CurriculumResponse addCurriculumToClassSubject(Long classSubjectId, CurriculumRequest request);

    // Update an existing curriculum
    CurriculumResponse updateCurriculum(Long curriculumId, CurriculumRequest request);

    // Get curriculum details by ID
    CurriculumResponse getCurriculumById(Long curriculumId);

    // Get all curriculums
    List<CurriculumResponse> getAllCurriculums();

    // Delete a curriculum by ID
    void deleteCurriculum(Long curriculumId);
}
