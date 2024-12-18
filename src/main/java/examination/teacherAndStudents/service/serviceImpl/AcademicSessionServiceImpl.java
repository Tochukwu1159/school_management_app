package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.AcademicSessionRequest;
import examination.teacherAndStudents.dto.AcademicSessionResponse;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.StudentTermRepository;
import examination.teacherAndStudents.service.AcademicSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicSessionServiceImpl implements AcademicSessionService {

    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;

    public AcademicSessionResponse  createAcademicSession(AcademicSessionRequest request) {
        AcademicSession session = AcademicSession.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        AcademicSession savedSession = academicSessionRepository.save(session);
        createStudentTerms(request,savedSession);
        return mapToResponse(savedSession);
    }

    public AcademicSessionResponse updateAcademicSession(Long id, AcademicSessionRequest request) {
        AcademicSession session = academicSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + id));
        session.setName(request.getName());
        session.setStartDate(request.getStartDate());
        session.setEndDate(request.getEndDate());
        AcademicSession updatedSession = academicSessionRepository.save(session);
        return mapToResponse(updatedSession);
    }

    public AcademicSessionResponse getAcademicSessionById(Long id) {
        AcademicSession session = academicSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + id));
        return mapToResponse(session);
    }

    public List<AcademicSessionResponse> getAllAcademicSessions() {
        return academicSessionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteAcademicSession(Long id) {
        academicSessionRepository.deleteById(id);
    }

    private AcademicSessionResponse mapToResponse(AcademicSession session) {
        return AcademicSessionResponse.builder()
                .id(session.getId())
                .name(session.getName())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .build();
    }

    private void createStudentTerms(AcademicSessionRequest academicSessionRequest, AcademicSession session) {

        List<StudentTerm> terms = Arrays.asList(
                StudentTerm.builder().name("First Term")
                        .startDate(academicSessionRequest.getFirstTermStartDate())
                        .endDate(academicSessionRequest.getFirstTermEndDate())
                        .academicSession(session).build(),

                StudentTerm.builder().name("Second Term")
                        .startDate(academicSessionRequest.getSecondTermStartDate())
                        .endDate(academicSessionRequest.getSecondTermEndDate())
                        .academicSession(session).build(),

                StudentTerm.builder().name("Third Term")
                        .startDate(academicSessionRequest.getThirdTermStartDate())
                        .endDate(academicSessionRequest.getThirdTermEndDate())
                        .academicSession(session).build()
        );
        studentTermRepository.saveAll(terms);
    }

}
