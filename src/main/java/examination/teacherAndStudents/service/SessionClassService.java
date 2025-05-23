package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SessionClassRequest;
import examination.teacherAndStudents.dto.SessionClassResponse;
import org.springframework.data.domain.Page;

public interface SessionClassService {

    SessionClassResponse addProfilesToSessionClass(SessionClassRequest request);

    SessionClassResponse getSessionClassById(Long id);

    Page<SessionClassResponse> getAllSessionClasses(Long sessionId, Long classBlockId, int page, int size, String sortBy, String sortDirection);

    SessionClassResponse updateSessionClass(Long id, SessionClassRequest request);

    void deleteSessionClass(Long id);

    SessionClassResponse assignAssignmentToSessionClass(Long sessionClassId, Long assignmentId);
}