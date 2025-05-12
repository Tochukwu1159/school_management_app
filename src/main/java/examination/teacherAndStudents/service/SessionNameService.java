package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SessionNameRequest;
import examination.teacherAndStudents.dto.SessionNameResponse;
import org.springframework.data.domain.Page;

public interface SessionNameService {
    SessionNameResponse createSessionName(SessionNameRequest request);

    SessionNameResponse updateSessionName(Long id, SessionNameRequest request);

    SessionNameResponse getSessionNameById(Long id);
    Page<SessionNameResponse> getAllSessionNames(int page, int size, String sortBy, String sortDirection);
    void deleteSessionName(Long id);

}