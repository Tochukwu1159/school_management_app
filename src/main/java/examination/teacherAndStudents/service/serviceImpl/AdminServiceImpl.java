package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.AdminService;
import examination.teacherAndStudents.utils.EntityFetcher;
import examination.teacherAndStudents.utils.ProfileStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final EntityFetcher entityFetcher;
    @Override
    public Page<User> getAllStudents(
            String firstName,
            String lastName,
            String middleName,
            String email,
            ProfileStatus profileStatus,
            Long id,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            String loggedEmail = entityFetcher.fetchLoggedInUser();
            User admin = entityFetcher.fetchLoggedInAdmin(loggedEmail);

            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            return userRepository.findAllStudentsWithFilters(
                    admin.getSchool().getId(),
                    firstName,
                    lastName,
                    middleName,
                    email,
                    profileStatus,
                    id,
                    pageable);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching students: " + e.getMessage());
        }
    }

        @Override
        public List<User> getAllTeachers () {
            try {
                String email = SecurityConfig.getAuthenticatedUserEmail();
                User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                        .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

                return userRepository.findUsersByRole(Roles.TEACHER);
            } catch (Exception e) {
                throw new CustomInternalServerException("An error occurred while fetching teachers "+e.getMessage());
            }
        }

    }