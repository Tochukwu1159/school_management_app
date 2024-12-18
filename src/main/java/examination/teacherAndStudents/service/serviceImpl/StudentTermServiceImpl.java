
package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.StudentTermRequest;
import examination.teacherAndStudents.dto.StudentTermResponse;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.StudentTermRepository;
import examination.teacherAndStudents.service.StudentTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentTermServiceImpl implements StudentTermService {

    private final StudentTermRepository studentTermRepository;
    private final AcademicSessionRepository academicSessionRepository;


    public StudentTermResponse createStudentTerm(StudentTermRequest request) {
        AcademicSession session = academicSessionRepository.findById(request.getAcademicSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid academic session ID"));

        StudentTerm term = StudentTerm.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .academicSession(session)
                .build();

        StudentTerm savedTerm = studentTermRepository.save(term);
        return mapToResponse(savedTerm);
    }
    public StudentTermResponse updateStudentTerm(Long id, StudentTermRequest request) {
        StudentTerm term = studentTermRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student term not found"));

        AcademicSession session = academicSessionRepository.findById(request.getAcademicSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid academic session ID"));

        term.setName(request.getName());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        term.setAcademicSession(session);

        StudentTerm updatedTerm = studentTermRepository.save(term);
        return mapToResponse(updatedTerm);
    }

    public void deleteStudentTerm(Long id) {
        studentTermRepository.deleteById(id);
    }

    public List<StudentTermResponse> getAllStudentTerms() {
        return studentTermRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    public StudentTermResponse getStudentTermById(Long id) {
        StudentTerm term = studentTermRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student term not found"));
        return mapToResponse(term);
    }

    private StudentTermResponse mapToResponse(StudentTerm term) {
        return StudentTermResponse.builder()
                .id(term.getId())
                .name(term.getName())
                .startDate(term.getStartDate())
                .endDate(term.getEndDate())
                .academicSessionId(term.getAcademicSession().getId())
                .build();
    }
}
