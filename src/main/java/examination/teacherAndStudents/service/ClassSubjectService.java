package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassSubjectRequest;
import examination.teacherAndStudents.dto.ClassSubjectResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ClassSubjectService {
    ClassSubjectResponse saveClassSubject(ClassSubjectRequest request);
    Page<ClassSubjectResponse> getAllClassSubjects(
            Long academicYearId,
            Long subjectId,
            Long classSubjectId,
            String subjectName,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    ClassSubjectResponse getClassSubjectById(Long id);
    void deleteClassSubject(Long id);
}
