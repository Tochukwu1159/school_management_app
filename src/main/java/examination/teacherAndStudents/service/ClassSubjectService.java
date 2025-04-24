package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ClassSubjectRequest;
import examination.teacherAndStudents.dto.ClassSubjectResponse;
import examination.teacherAndStudents.dto.TeacherAssignmentRequest;
import org.springframework.data.domain.Page;

public interface ClassSubjectService {
    ClassSubjectResponse saveClassSubject(ClassSubjectRequest request);
    ClassSubjectResponse getClassSubjectById(Long id);
    Page<ClassSubjectResponse> getAllClassSubjects(
            Long academicYearId, Long subjectId, Long classSubjectId, String subjectName,
            int page, int size, String sortBy, String sortDirection);
    void deleteClassSubject(Long id);
    void assignClassSubjectToTeacher(TeacherAssignmentRequest request);
    void updateClassSubjectTeacherAssignment(TeacherAssignmentRequest request);
}