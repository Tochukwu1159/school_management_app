package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SubjectRequest;
import examination.teacherAndStudents.dto.SubjectResponse;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.SubjectRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.SubjectService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
    private static final Logger log = LoggerFactory.getLogger(SubjectServiceImpl.class);

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    private User validateAuthenticatedUser(boolean requireAdmin) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        if (email == null) {
            throw new CustomNotFoundException("No authenticated user found");
        }

        Set<Roles> allowedRoles = requireAdmin ? Set.of(Roles.ADMIN) : Set.of(Roles.ADMIN, Roles.TEACHER);
        User user = userRepository.findByEmailAndRolesIn(email, allowedRoles)
                .orElseThrow(() -> new CustomNotFoundException(
                        "Please login as an " + (requireAdmin ? "Admin" : "Admin or Teacher")));
        School school = user.getSchool();
        if (school == null) {
            throw new CustomInternalServerException("User is not associated with any school");
        }
        return user;
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "name";
        }
        if (!List.of("id", "name").contains(sortBy)) {
            log.warn("Unknown sort field '{}', defaulting to 'name'", sortBy);
            sortBy = "name";
        }
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection + ". Use 'ASC' or 'DESC'");
        }
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    @Transactional
    @Override
    public SubjectResponse createSubject(SubjectRequest subjectRequest) {
        try {
            User admin = validateAuthenticatedUser(true);
            School school = admin.getSchool();

            if (subjectRequest.getName() == null || subjectRequest.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Subject name must not be null or empty");
            }

            // Check for existing subject
            if (subjectRepository.existsBySchoolIdAndName(school.getId(), subjectRequest.getName().trim())) {
                throw new IllegalArgumentException("Subject '" + subjectRequest.getName() + "' already exists in this school");
            }

            Subject subject = Subject.builder()
                    .name(subjectRequest.getName().trim())
                    .school(school)
                    .build();

            Subject savedSubject = subjectRepository.save(subject);
            log.info("Created subject ID {} for school ID {}", savedSubject.getId(), school.getId());
            return modelMapper.map(savedSubject, SubjectResponse.class);
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error creating subject: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating subject: {}", e.getMessage(), e);
            throw new CustomInternalServerException("Failed to create subject: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public SubjectResponse updateSubject(Long subjectId, SubjectRequest subjectRequest) {
        try {
            User admin = validateAuthenticatedUser(true);
            School school = admin.getSchool();

            if (subjectRequest.getName() == null || subjectRequest.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Subject name must not be null or empty");
            }

            Subject existingSubject = subjectRepository.findByIdAndSchoolId(subjectId, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException(
                            "Subject not found with ID " + subjectId + " in school ID " + school.getId()));

            existingSubject.setName(subjectRequest.getName().trim());
            Subject updatedSubject = subjectRepository.save(existingSubject);

            log.info("Updated subject ID {} for school ID {}", subjectId, school.getId());
            return modelMapper.map(updatedSubject, SubjectResponse.class);
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error updating subject ID {}: {}", subjectId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating subject ID {}: {}", subjectId, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to update subject ID " + subjectId + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public SubjectResponse findSubjectById(Long subjectId) {
        try {
            User user = validateAuthenticatedUser(false);
            School school = user.getSchool();

            Subject subject = subjectRepository.findByIdAndSchoolId(subjectId, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException(
                            "Subject not found with ID " + subjectId + " in school ID " + school.getId()));

            log.info("Fetched subject ID {} for school ID {}", subjectId, school.getId());
            return modelMapper.map(subject, SubjectResponse.class);
        } catch (CustomNotFoundException e) {
            log.error("Error fetching subject ID {}: {}", subjectId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching subject ID {}: {}", subjectId, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to fetch subject ID " + subjectId + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SubjectResponse> findAllSubjects(String name, int page, int size, String sortBy, String sortDirection) {
        try {
            User user = validateAuthenticatedUser(false);
            School school = user.getSchool();

            Pageable pageable = createPageable(page, size, sortBy, sortDirection);
            Page<Subject> subjects = subjectRepository.findAllBySchoolIdAndNameContaining(
                    school.getId(), name != null ? name.trim() : "", pageable);

            log.info("Fetched {} subjects for school ID {}", subjects.getTotalElements(), school.getId());
            return subjects.map(subject -> modelMapper.map(subject, SubjectResponse.class));
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error fetching subjects: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching subjects: {}", e.getMessage(), e);
            throw new CustomInternalServerException("Failed to fetch subjects: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deleteSubject(Long subjectId) {
        try {
            User admin = validateAuthenticatedUser(true);
            School school = admin.getSchool();

            Subject subject = subjectRepository.findByIdAndSchoolId(subjectId, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException(
                            "Subject not found with ID " + subjectId + " in school ID " + school.getId()));

            // Check for dependencies (optional)
            if (subjectRepository.hasDependencies(subjectId)) {
                throw new IllegalStateException("Cannot delete subject ID " + subjectId + " due to existing dependencies");
            }

            subjectRepository.delete(subject);
            log.info("Deleted subject ID {} for school ID {}", subjectId, school.getId());
        } catch (CustomNotFoundException | IllegalStateException e) {
            log.error("Error deleting subject ID {}: {}", subjectId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting subject ID {}: {}", subjectId, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to delete subject ID " + subjectId + ": " + e.getMessage());
        }
    }
}