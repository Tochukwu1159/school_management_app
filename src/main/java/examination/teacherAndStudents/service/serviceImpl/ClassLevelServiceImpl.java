package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ClassLevelRequest;
import examination.teacherAndStudents.dto.ClassLevelRequestUrl;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassLevel;
import examination.teacherAndStudents.entity.Rating;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.ClassLevelRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.ClassLevelService;
import examination.teacherAndStudents.utils.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassLevelServiceImpl implements ClassLevelService {

    private final ClassLevelRepository classLevelRepository;
    private final UserRepository userRepository;
    private final AcademicSessionRepository academicSessionRepository;

    @Autowired
    public ClassLevelServiceImpl(ClassLevelRepository classLevelRepository,
                                 UserRepository userRepository, AcademicSessionRepository academicSessionRepository) {
        this.classLevelRepository = classLevelRepository;
        this.userRepository = userRepository;
        this.academicSessionRepository = academicSessionRepository;
    }

    public List<ClassLevel> getAllClassLevels() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }
            return classLevelRepository.findAll();
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching all classes " + e);

        }
    }


    public Optional<ClassLevel> getClassLevelById(Long id) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Optional<ClassLevel> classLevelOptional = classLevelRepository.findById(id);

            if (classLevelOptional.isPresent()) {
                return classLevelOptional;
            } else {
                throw new NotFoundException("Class level with ID " + id + " not found");
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            throw new CustomInternalServerException("Error fetching class by ID " +e);
        }
    }

    public ClassLevel createClassLevel(ClassLevelRequest classLevel) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Optional<User> userDetails = userRepository.findByEmail(email);
            AcademicSession academicSession = academicSessionRepository.findById(classLevel.getAcademicSessionId())
                    .orElseThrow(() -> new NotFoundException("Academic session not found"));

            ClassLevel newClass = new ClassLevel();
            newClass.setClassName(classLevel.getClassName());
            newClass.setAcademicYear(academicSession);
            newClass.setSchool(userDetails.get().getSchool());
            return classLevelRepository.save(newClass);

        } catch (Exception e) {
            // Log the exception or handle it according to your requirements
            throw new CustomInternalServerException("Error creating class " + e);
        }
    }

    public ClassLevel updateClassLevel(Long id, ClassLevelRequest classLevelRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Optional<ClassLevel> existingClassLevel = classLevelRepository.findById(id);

            if (existingClassLevel.isPresent()) {
                ClassLevel updatedClassLevel = existingClassLevel.get();
                updatedClassLevel.setClassName(classLevelRequest.getClassName());
                return classLevelRepository.save(updatedClassLevel);
            } else {
                throw new NotFoundException("Class level with ID " + id + " not found");
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Error updating class  " + e);
        }
    }

    public ClassLevel updateClassLevelUrl(Long id, ClassLevelRequestUrl classLevelRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            Optional<ClassLevel> existingClassLevel = classLevelRepository.findById(id);

            if (existingClassLevel.isPresent()) {
                ClassLevel updatedClassLevel = existingClassLevel.get();
                updatedClassLevel.setClassName(classLevelRequest.getClassUniqueUrl());
                return classLevelRepository.save(updatedClassLevel);
            } else {
                throw new NotFoundException("Class level with ID " + id + " not found");
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Error updating class unique link  " + e);
        }
    }

    public void deleteClassLevel(Long id) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }

            if (classLevelRepository.existsById(id)) {
                classLevelRepository.deleteById(id);
            } else {
                throw new NotFoundException("Class level with ID " + id + " not found");
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Error deleting class "+ e);
        }
    }
}