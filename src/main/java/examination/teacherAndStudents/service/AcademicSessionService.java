package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.AcademicSessionRequest;
import examination.teacherAndStudents.dto.AcademicSessionResponse;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.utils.SessionPromotion;
import examination.teacherAndStudents.utils.SessionStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface AcademicSessionService {
    AcademicSessionResponse createAcademicSession(AcademicSessionRequest request);
    AcademicSessionResponse updateAcademicSession(Long id, AcademicSessionRequest request);
    AcademicSessionResponse getAcademicSessionById(Long id);
    Page<AcademicSessionResponse> getAllAcademicSessions(
            String name,
            SessionStatus status,
            SessionPromotion promotion,
            Long id,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    void deleteAcademicSession(Long id);
    void graduateStudentsForSession(Long academicSessionId, List<Long> classBlockIds);
}
