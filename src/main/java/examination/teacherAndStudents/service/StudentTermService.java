package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StudentTermRequest;
import examination.teacherAndStudents.dto.StudentTermResponse;

import java.util.List;

public interface StudentTermService {
    StudentTermResponse createStudentTerm(StudentTermRequest request);
    StudentTermResponse updateStudentTerm(Long id, StudentTermRequest request);
    void deleteStudentTerm(Long id);
    List<StudentTermResponse> getAllStudentTerms();
    StudentTermResponse getStudentTermById(Long id);
}
