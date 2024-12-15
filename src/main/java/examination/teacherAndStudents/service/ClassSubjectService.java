package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassSubjectRequest;
import examination.teacherAndStudents.dto.ClassSubjectResponse;

import java.util.List;

public interface ClassSubjectService {
    ClassSubjectResponse saveClassSubject(ClassSubjectRequest request);
    List<ClassSubjectResponse> getAllClassSubjects();
    ClassSubjectResponse getClassSubjectById(Long id);
    void deleteClassSubject(Long id);
}
