package examination.teacherAndStudents.service.serviceImpl;


import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SessionNameRequest;
import examination.teacherAndStudents.dto.SessionNameResponse;
import examination.teacherAndStudents.entity.SessionName;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.repository.SessionNameRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.SessionNameService;
import examination.teacherAndStudents.utils.EntityFetcher;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionNameServiceImpl implements SessionNameService {


    private final SessionNameRepository sessionNameRepository;
    private final UserRepository userRepository;
    private final EntityFetcher entityFetcher;

    @Override
    public SessionNameResponse createSessionName(SessionNameRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = entityFetcher.fetchLoggedInAdmin(email);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }

        SessionName sessionName = SessionName.builder()
                .name(request.getName())
                .build();
        SessionName savedSessionName = sessionNameRepository.save(sessionName);
        return mapToResponse(savedSessionName);
    }

    @Override
    public SessionNameResponse updateSessionName(Long id, SessionNameRequest request) {
        SessionName sessionName = entityFetcher.fetchSessionName(id);
        sessionName.setName(request.getName());
        SessionName updatedSessionName = sessionNameRepository.save(sessionName);
        return mapToResponse(updatedSessionName);
    }

    @Override
    public SessionNameResponse getSessionNameById(Long id) {
        SessionName sessionName = entityFetcher.fetchSessionName(id);
        return mapToResponse(sessionName);
    }

    @Override
    public Page<SessionNameResponse> getAllSessionNames(int page, int size, String sortBy, String sortDirection) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Admin not found"));

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<SessionName> sessionNamesPage = sessionNameRepository.findAll(pageable);
            return sessionNamesPage.map(this::mapToResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching session names: " + e.getMessage());
        }
    }

    @Override
    public void deleteSessionName(Long id) {
        sessionNameRepository.deleteById(id);
    }

    private SessionNameResponse mapToResponse(SessionName sessionName) {
        return SessionNameResponse.builder()
                .id(sessionName.getId())
                .name(sessionName.getName())
                .build();
    }
}

