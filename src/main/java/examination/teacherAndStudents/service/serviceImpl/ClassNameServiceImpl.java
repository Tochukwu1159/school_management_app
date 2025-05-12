package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ClassNameRequest;
import examination.teacherAndStudents.dto.ClassNameResponse;
import examination.teacherAndStudents.entity.ClassName;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.repository.ClassNameRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.ClassNameService;
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
public class ClassNameServiceImpl implements ClassNameService {

    private final ClassNameRepository classNameRepository;
    private final UserRepository userRepository;
    private final EntityFetcher entityFetcher;

    @Override
    public ClassNameResponse createClassName(ClassNameRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = entityFetcher.fetchLoggedInAdmin(email);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }

        ClassName className = ClassName.builder()
                .name(request.getName())
                .build();
        ClassName savedClassName = classNameRepository.save(className);
        return mapToResponse(savedClassName);
    }

    @Override
    public ClassNameResponse updateClassName(Long id, ClassNameRequest request) {
        ClassName className = entityFetcher.fetchClassName(id);
        className.setName(request.getName());
        ClassName updatedClassName = classNameRepository.save(className);
        return mapToResponse(updatedClassName);
    }

    @Override
    public ClassNameResponse getClassNameById(Long id) {
        ClassName className = entityFetcher.fetchClassName(id);
        return mapToResponse(className);
    }

    @Override
    public Page<ClassNameResponse> getAllClassNames(String name, int page, int size, String sortBy, String sortDirection) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Admin not found"));

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ClassName> classNamesPage;
            if (name != null && !name.isEmpty()) {
                classNamesPage = classNameRepository.findByNameContainingIgnoreCase(name, pageable);
            } else {
                classNamesPage = classNameRepository.findAll(pageable);
            }
            return classNamesPage.map(this::mapToResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching class names: " + e.getMessage());
        }
    }

    @Override
    public void deleteClassName(Long id) {
        classNameRepository.deleteById(id);
    }

    private ClassNameResponse mapToResponse(ClassName className) {
        return ClassNameResponse.builder()
                .id(className.getId())
                .name(className.getName())
                .build();
    }
}