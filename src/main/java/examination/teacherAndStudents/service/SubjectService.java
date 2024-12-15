package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SubjectRequest;
import examination.teacherAndStudents.dto.SubjectResponse;
import examination.teacherAndStudents.entity.Subject;

import java.util.List;

public interface SubjectService {
    SubjectResponse createSubject(SubjectRequest subject);
    SubjectResponse updateSubject(Long subjectId, SubjectRequest updatedSubjectRequest);
    SubjectResponse findSubjectById(Long subjectId);
    List<SubjectResponse> findAllSubjects();


}
