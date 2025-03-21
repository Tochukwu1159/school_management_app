package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.AdminService;
import examination.teacherAndStudents.utils.EntityFetcher;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final EntityFetcher entityFetcher;
    @Override
    public List<User> getAllStudents() {
        try {
            String email = entityFetcher.fetchLoggedInUser();
            User admin = entityFetcher.fetchLoggedInAdmin(email);

            return userRepository.findUserByRoles(Roles.STUDENT);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching students " +e.getMessage());
        }
    }

        @Override
        public List<User> getAllTeachers () {
            try {
                String email = SecurityConfig.getAuthenticatedUserEmail();
                User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
                if (admin == null) {
                    throw new CustomNotFoundException("Please login as an Admin"); // Return unauthorized response for non-admin users
                }

                return userRepository.findUserByRoles(Roles.TEACHER);
            } catch (Exception e) {
                throw new CustomInternalServerException("An error occurred while fetching teachers "+e.getMessage());
            }
        }

    }