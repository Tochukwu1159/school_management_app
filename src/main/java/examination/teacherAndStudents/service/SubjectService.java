package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SubjectRequest;
import examination.teacherAndStudents.dto.SubjectResponse;
import examination.teacherAndStudents.entity.Subject;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SubjectService {
    SubjectResponse createSubject(SubjectRequest subject);
    SubjectResponse updateSubject(Long subjectId, SubjectRequest updatedSubjectRequest);
    SubjectResponse findSubjectById(Long subjectId);
    Page<SubjectResponse> findAllSubjects(String name, int page, int size, String sortBy, String sortDirection);
    void deleteSubject(Long subjectId);


}
