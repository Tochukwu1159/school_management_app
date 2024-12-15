package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.AcademicSessionRequest;
import examination.teacherAndStudents.dto.AcademicSessionResponse;

import java.util.List;

public interface AcademicSessionService {
    AcademicSessionResponse createAcademicSession(AcademicSessionRequest request);
    AcademicSessionResponse updateAcademicSession(Long id, AcademicSessionRequest request);
    AcademicSessionResponse getAcademicSessionById(Long id);
    List<AcademicSessionResponse> getAllAcademicSessions();
    void deleteAcademicSession(Long id);
}
