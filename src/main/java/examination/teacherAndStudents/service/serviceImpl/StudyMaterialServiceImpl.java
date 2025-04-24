package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.StudyMaterialRequest;
import examination.teacherAndStudents.dto.StudyMaterialResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.StudyMaterialService;
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
public class StudyMaterialServiceImpl implements StudyMaterialService {
    private static final Logger log = LoggerFactory.getLogger(StudyMaterialServiceImpl.class);

    private final StudyMaterialRepository studyMaterialRepository;
    private final SubjectRepository subjectRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final ClassBlockRepository classBlockRepository;
    private final ModelMapper modelMapper;

    private User validateAuthenticatedUser(boolean requireTeacher) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        if (email == null) {
            throw new CustomNotFoundException("No authenticated user found");
        }

        Set<Roles> allowedRoles = requireTeacher ? Set.of(Roles.TEACHER, Roles.ADMIN) :
                Set.of(Roles.TEACHER, Roles.STUDENT, Roles.ADMIN);
        User user = userRepository.findByEmailAndRolesIn(email, allowedRoles)
                .orElseThrow(() -> new CustomNotFoundException(
                        "Please login as an " + (requireTeacher ? "Teacher or Admin" : "Teacher, Student, or Admin")));
        School school = user.getSchool();
        if (school == null) {
            throw new CustomInternalServerException("User is not associated with any school");
        }
        return user;
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "title";
        }
        if (!List.of("id", "title", "createdAt").contains(sortBy)) {
            log.warn("Unknown sort field '{}', defaulting to 'title'", sortBy);
            sortBy = "title";
        }
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection + ". Use 'ASC' or 'DESC'");
        }
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StudyMaterialResponse> getAllMaterials(int page, int size, String sortBy, String sortDirection) {
        try {
            User user = validateAuthenticatedUser(false);
            School school = user.getSchool();

            Pageable pageable = createPageable(page, size, sortBy, sortDirection);
            Page<StudyMaterial> materials;
            boolean isTeacherOrAdmin = user.getRoles().contains(Roles.TEACHER) || user.getRoles().contains(Roles.ADMIN);
            if (isTeacherOrAdmin) {
                materials = studyMaterialRepository.findAllBySubjectSchoolId(school.getId(), pageable);
            } else {
                Profile profile = profileRepository.findByUserEmail(user.getEmail())
                        .orElseThrow(() -> new CustomNotFoundException("Profile not found for user: " + user.getEmail()));
                materials = studyMaterialRepository.findAllByClassBlockStudentsProfileIdAndSubjectSchoolId(
                        profile.getId(), school.getId(), pageable);
            }

            log.info("Fetched {} study materials for user {} in school ID {}", materials.getTotalElements(), user.getEmail(), school.getId());
            return materials.map(this::toResponse);
        } catch (CustomNotFoundException | IllegalArgumentException e) {
            log.error("Error fetching study materials: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching study materials: {}", e.getMessage(), e);
            throw new CustomInternalServerException("Failed to fetch study materials: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public StudyMaterialResponse getMaterialById(Long id) {
        try {
            User user = validateAuthenticatedUser(false);
            School school = user.getSchool();

            StudyMaterial material = studyMaterialRepository.findByIdAndSubjectSchoolId(id, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Study material not found with ID: " + id + " in school ID: " + school.getId()));

            boolean isTeacherOrAdmin = user.getRoles().contains(Roles.TEACHER) || user.getRoles().contains(Roles.ADMIN);
            if (!isTeacherOrAdmin) {
                Profile profile = profileRepository.findByUserEmail(user.getEmail())
                        .orElseThrow(() -> new CustomNotFoundException("Profile not found for user: " + user.getEmail()));
                if (!material.getClassBlock().getStudentList().contains(profile)) {
                    throw new CustomNotFoundException("You are not authorized to view study material ID: " + id);
                }
            }

            log.info("Fetched study material ID {} for user {} in school ID {}", id, user.getEmail(), school.getId());
            return toResponse(material);
        } catch (CustomNotFoundException e) {
            log.error("Error fetching study material ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching study material ID {}: {}", id, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to fetch study material ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public StudyMaterialResponse saveMaterial(StudyMaterialRequest request) {
        try {
            User user = validateAuthenticatedUser(true);
            School school = user.getSchool();

            validateRequest(request);

            Profile teacher = profileRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new CustomNotFoundException("Teacher not found with ID: " + request.getTeacherId()));
            if (!school.equals(teacher.getUser().getSchool())) {
                throw new CustomNotFoundException("Teacher does not belong to your school");
            }

            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new CustomNotFoundException("Subject not found with ID: " + request.getSubjectId()));
            if (!school.equals(subject.getSchool())) {
                throw new CustomNotFoundException("Subject does not belong to your school");
            }

            AcademicSession academicYear = academicSessionRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new CustomNotFoundException("Academic Year not found with ID: " + request.getAcademicYearId()));
            StudentTerm term = studentTermRepository.findById(request.getTermId())
                    .orElseThrow(() -> new CustomNotFoundException("Student Term not found with ID: " + request.getTermId()));
            ClassBlock classBlock = classBlockRepository.findById(request.getClassId())
                    .orElseThrow(() -> new CustomNotFoundException("Class not found with ID: " + request.getClassId()));
            if (!school.equals(classBlock.getClassLevel().getSchool())) {
                throw new CustomNotFoundException("Class does not belong to your school");
            }

            StudyMaterial material = StudyMaterial.builder()
                    .title(request.getTitle().trim())
                    .filePath(request.getFilePath().trim())
                    .subject(subject)
                    .teacher(teacher)
                    .academicYear(academicYear)
                    .studentTerm(term)
                    .classBlock(classBlock)
                    .build();

            StudyMaterial savedMaterial = studyMaterialRepository.save(material);
            log.info("Saved study material ID {} by user {} in school ID {}", savedMaterial.getId(), user.getEmail(), school.getId());
            return toResponse(savedMaterial);
        } catch (CustomNotFoundException | IllegalArgumentException e) {
            log.error("Error saving study material: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error saving study material: {}", e.getMessage(), e);
            throw new CustomInternalServerException("Failed to save study material: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public StudyMaterialResponse updateMaterial(Long id, StudyMaterialRequest request) {
        try {
            User user = validateAuthenticatedUser(true);
            School school = user.getSchool();

            validateRequest(request);

            StudyMaterial material = studyMaterialRepository.findByIdAndSubjectSchoolId(id, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Study material not found with ID: " + id + " in school ID: " + school.getId()));

            Profile teacher = profileRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new CustomNotFoundException("Teacher not found with ID: " + request.getTeacherId()));
            if (!school.equals(teacher.getUser().getSchool())) {
                throw new CustomNotFoundException("Teacher does not belong to your school");
            }

            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new CustomNotFoundException("Subject not found with ID: " + request.getSubjectId()));
            if (!school.equals(subject.getSchool())) {
                throw new CustomNotFoundException("Subject does not belong to your school");
            }

            AcademicSession academicYear = academicSessionRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new CustomNotFoundException("Academic Year not found with ID: " + request.getAcademicYearId()));
            StudentTerm term = studentTermRepository.findById(request.getTermId())
                    .orElseThrow(() -> new CustomNotFoundException("Student Term not found with ID: " + request.getTermId()));
            ClassBlock classBlock = classBlockRepository.findById(request.getClassId())
                    .orElseThrow(() -> new CustomNotFoundException("Class not found with ID: " + request.getClassId()));
            if (!school.equals(classBlock.getClassLevel().getSchool())) {
                throw new CustomNotFoundException("Class does not belong to your school");
            }

            material.setTitle(request.getTitle().trim());
            material.setFilePath(request.getFilePath().trim());
            material.setSubject(subject);
            material.setTeacher(teacher);
            material.setAcademicYear(academicYear);
            material.setStudentTerm(term);
            material.setClassBlock(classBlock);

            StudyMaterial updatedMaterial = studyMaterialRepository.save(material);
            log.info("Updated study material ID {} by user {} in school ID {}", id, user.getEmail(), school.getId());
            return toResponse(updatedMaterial);
        } catch (CustomNotFoundException | IllegalArgumentException e) {
            log.error("Error updating study material ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating study material ID {}: {}", id, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to update study material ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deleteMaterial(Long id) {
        try {
            User user = validateAuthenticatedUser(true);
            School school = user.getSchool();

            StudyMaterial material = studyMaterialRepository.findByIdAndSubjectSchoolId(id, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Study material not found with ID: " + id + " in school ID: " + school.getId()));

            studyMaterialRepository.delete(material);
            log.info("Deleted study material ID {} by user {} in school ID {}", id, user.getEmail(), school.getId());
        } catch (CustomNotFoundException e) {
            log.error("Error deleting study material ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting study material ID {}: {}", id, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to delete study material ID " + id + ": " + e.getMessage());
        }
    }

    private void validateRequest(StudyMaterialRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title must not be null or empty");
        }
        if (request.getFilePath() == null || request.getFilePath().trim().isEmpty()) {
            throw new IllegalArgumentException("File path must not be null or empty");
        }
        if (!request.getFilePath().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("File must be a PDF");
        }
        if (request.getSubjectId() == null) {
            throw new IllegalArgumentException("Subject ID must not be null");
        }
        if (request.getTeacherId() == null) {
            throw new IllegalArgumentException("Teacher ID must not be null");
        }
        if (request.getAcademicYearId() == null) {
            throw new IllegalArgumentException("Academic Year ID must not be null");
        }
        if (request.getTermId() == null) {
            throw new IllegalArgumentException("Term ID must not be null");
        }
        if (request.getClassId() == null) {
            throw new IllegalArgumentException("Class ID must not be null");
        }
    }

    private StudyMaterialResponse toResponse(StudyMaterial material) {
        StudyMaterialResponse response = modelMapper.map(material, StudyMaterialResponse.class);
        response.setSubjectId(material.getSubject().getId());
        response.setTeacherId(material.getTeacher().getId());
        response.setAcademicYearId(material.getAcademicYear().getId());
        response.setTermId(material.getStudentTerm().getId());
        response.setClassId(material.getClassBlock().getId());
        return response;
    }
}