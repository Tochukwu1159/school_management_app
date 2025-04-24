package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StudentTermDetailedResponse;
import examination.teacherAndStudents.dto.StudentTermRequest;
import examination.teacherAndStudents.dto.StudentTermResponse;
import examination.teacherAndStudents.utils.TermStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentTermService {
    StudentTermDetailedResponse createStudentTerm(StudentTermRequest request);
    StudentTermDetailedResponse updateStudentTerm(Long id, StudentTermRequest request);
    void deleteStudentTerm(Long id);
    Page<StudentTermDetailedResponse> getAllStudentTerms(Pageable pageable);
    StudentTermDetailedResponse getStudentTermById(Long id);
    List<StudentTermDetailedResponse> getStudentTermsBySession(Long sessionId);
    void updateTermStatus(Long id, TermStatus status);
}
