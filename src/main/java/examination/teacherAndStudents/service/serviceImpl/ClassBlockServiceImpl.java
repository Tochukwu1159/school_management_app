package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ClassBlockRequest;
import examination.teacherAndStudents.dto.ClassBlockResponse;
import examination.teacherAndStudents.dto.FormTeacherAssignmentRequest;
import examination.teacherAndStudents.dto.UpdateFormTeacherRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ClassBlockService;
import examination.teacherAndStudents.utils.EntityFetcher;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassBlockServiceImpl implements ClassBlockService {

    private final ClassBlockRepository classBlockRepository;
    private final ClassLevelRepository classLevelRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final SessionClassRepository sessionClassRepository;
    private final EntityFetcher entityFetcher;

    public ClassBlockResponse getClassBlockById(Long id) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = entityFetcher.fetchLoggedInAdmin(email);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }
            // Fetch the class block by ID
            ClassBlock classBlock = classBlockRepository.findByIdAndSchoolId(id, admin.getSchool().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class Block not found with ID: " + id));

            // Map the entity to a response DTO and return
            return mapToResponse(classBlock);
        } catch (ResourceNotFoundException e) {
            // Rethrow custom exceptions for clarity
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            throw new RuntimeException(  e.getMessage());
        }
    }


    public List<ClassBlockResponse> getAllClassBlocks(
            Long classId,
            Long subClassId,
            Long academicYearId) {
        try {
            // Fetch filtered class blocks from the repository
            List<ClassBlock> classBlocks = classBlockRepository.findAllWithFilters(
                    classId,
                    subClassId);

            // Map each class block to its response DTO
            return classBlocks.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Handle unexpected exceptions
            throw new RuntimeException("An error occurred while retrieving class blocks: " + e.getMessage());
        }
    }

    public ClassBlockResponse updateClassBlock(Long id, ClassBlockRequest request) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = entityFetcher.fetchLoggedInAdmin(email);
            if (admin == null) {
                throw new AuthenticationFailedException("Please login as an Admin");
            }
            // Fetch the class block by ID
            ClassBlock classBlock = classBlockRepository.findByIdAndSchoolId(id, admin.getSchool().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class Block not found"));

            // Update the class block details
            classBlock.setClassUniqueUrl(request.getClassUniqueUrl());

            // Save the updated class block
            classBlockRepository.save(classBlock);

            // Return the updated class block as a response
            return mapToResponse(classBlock);
        } catch (ResourceNotFoundException e) {
            // Handle the specific exception if the class block is not found
            throw new RuntimeException( e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected exceptions
            throw new RuntimeException(e.getMessage());
        }
    }



    public void deleteClassBlock(Long id) {
        try {
            // Fetch the class block by ID
            ClassBlock classBlock = classBlockRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Class Block not found"));

            // Delete the class block
            classBlockRepository.delete(classBlock);
        } catch (ResourceNotFoundException e) {
            // Handle not found exceptions
            throw new RuntimeException( e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected exceptions
            throw new RuntimeException(  e.getMessage());
        }
    }


    public ClassBlockResponse updateFormTeacher(UpdateFormTeacherRequest request) {
        try {
            academicSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
            // Fetch the class block
            ClassBlock classBlock = classBlockRepository.findByIdAndClassLevelId(request.getSubclassId(),request.getClassLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class  not found or does not exist in the class level"));

            // Fetch the new form teacher's profile
            Profile newFormTeacher = profileRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Form Teacher not found"));

            // Update the form teacher
            classBlock.setFormTeacher(newFormTeacher);
            classBlockRepository.save(classBlock);

            // Return the response
            return mapToResponse(classBlock);
        } catch (ResourceNotFoundException e) {
            // Handle not found exceptions
            throw new RuntimeException("Resource not found: " + e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected exceptions
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void assignFormTeachersToClassBlocks(FormTeacherAssignmentRequest request) {
        verifyAdminAccess();

        // Validate AcademicSession
        AcademicSession session = academicSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomNotFoundException("Academic Session not found with ID: " + request.getSessionId()));

       classLevelRepository.findById(request.getClassLevelId())
                .orElseThrow(() -> new CustomNotFoundException("Class Level not found "));

        // Validate assignments list
        if (request.getAssignments() == null || request.getAssignments().isEmpty()) {
            throw new IllegalArgumentException("Assignments list cannot be empty");
        }

        for (FormTeacherAssignmentRequest.Assignment assignment : request.getAssignments()) {
            // Validate ClassBlock
            ClassBlock classBlock = classBlockRepository.findById(assignment.getClassBlockId())
                    .orElseThrow(() -> new CustomNotFoundException("Class Block not found with ID: " + assignment.getClassBlockId()));

            // Validate that ClassBlock belongs to the specified ClassLevel
            if (!classBlock.getClassLevel().getId().equals(request.getClassLevelId())) {
                throw new IllegalArgumentException("Class Block with ID: " + assignment.getClassBlockId() + " does not belong to the specified Class Level");
            }

            // Validate Teacher
            User teacher = userRepository.findById(assignment.getTeacherId())
                    .orElseThrow(() -> new CustomNotFoundException("Teacher with ID: " + assignment.getTeacherId() + " not found"));

            Profile teacherProfile = profileRepository.findByUser(teacher)
                    .orElseThrow(() -> new CustomNotFoundException("Teacher profile with ID: " + assignment.getTeacherId() + " not found"));

            if (!teacher.getRoles().contains(Roles.TEACHER)) {
                throw new IllegalArgumentException("User with ID: " + assignment.getTeacherId() + " is not a teacher");
            }

            // Check if the ClassBlock already has a form teacher assigned
            if (classBlock.getFormTeacher() != null) {
                throw new EntityAlreadyExistException("Class Block with ID: " + assignment.getClassBlockId() + " already has a form teacher assigned");
            }

            // Assign form teacher
            classBlock.setFormTeacher(teacherProfile);
            classBlock.setUpdatedAt(LocalDateTime.now());
            classBlockRepository.save(classBlock);
            log.info("Assigned form teacher [teacherId={}] to ClassBlock [classBlockId={}]", teacher.getId(), classBlock.getId());
        }
    }

    public ClassBlockResponse changeStudentClass(Long studentId, ClassBlockRequest request) {
        try {
            // Validate request
            if (request.getSessionId() == null || request.getCurrentClassBlockId() == null || request.getNewClassBlockId() == null) {
                throw new IllegalArgumentException("Session ID, current ClassBlock ID, and new ClassBlock ID are required");
            }

            // Fetch the student user entity
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

            // Fetch the student's profile
            Profile studentProfile = profileRepository.findByUser(student)
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + studentId));

            // Fetch the academic session
            AcademicSession academicSession = academicSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + request.getSessionId()));

            // Fetch the current class block
            ClassBlock currentClassBlock = classBlockRepository.findById(request.getCurrentClassBlockId())
                    .orElseThrow(() -> new ResourceNotFoundException("Current ClassBlock not found with ID: " + request.getCurrentClassBlockId()));

            // Fetch or create the current session class
            SessionClass currentSessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(request.getSessionId(), request.getCurrentClassBlockId()).orElseThrow(()->new ResourceNotFoundException("Current ClassBlock not found with ID: " + request.getCurrentClassBlockId()));

            // Verify the student is in the current session class
            if (studentProfile.getSessionClass() == null || !studentProfile.getSessionClass().getId().equals(currentSessionClass.getId())) {
                throw new IllegalArgumentException("Student is not currently in the specified session class (ID: " + currentSessionClass.getId() + ")");
            }

            // Fetch the new class block
            ClassBlock newClassBlock = classBlockRepository.findById(request.getNewClassBlockId())
                    .orElseThrow(() -> new ResourceNotFoundException("New ClassBlock not found with ID: " + request.getNewClassBlockId()));

            // Fetch or create the new session class

            SessionClass newSessionClass = sessionClassRepository.findBySessionIdAndClassBlockId(request.getSessionId(), request.getNewClassBlockId())
                    .orElseGet(() -> sessionClassRepository.save(SessionClass.builder()
                            .academicSession(academicSession)
                            .classBlock(newClassBlock)
                            .profiles(new HashSet<>())
                            .numberOfProfiles(0)
                            .build()));


            // Update the student counts and profiles for the session classes
            synchronized (this) {
                currentSessionClass.setNumberOfProfiles(currentSessionClass.getNumberOfProfiles() - 1);
                currentSessionClass.getProfiles().remove(studentProfile);
                newSessionClass.setNumberOfProfiles(newSessionClass.getNumberOfProfiles() + 1);
                newSessionClass.getProfiles().add(studentProfile);
                sessionClassRepository.save(currentSessionClass);
                sessionClassRepository.save(newSessionClass);
            }

            // Update the student's session class
            studentProfile.setSessionClass(newSessionClass);
            profileRepository.save(studentProfile);

            // Return the response
            return mapToResponse(newSessionClass.getClassBlock());
        } catch (Exception e) {
            throw new RuntimeException( e.getMessage());
        }
    }

    private ClassBlockResponse mapToResponse(ClassBlock classBlock) {
        return ClassBlockResponse.builder()
                .id(classBlock.getId())
                .name(classBlock.getName())
                .classLevelId(classBlock.getClassLevel().getId())
                .classUniqueUrl(classBlock.getClassUniqueUrl())
//                .numberOfStudents(classBlock.getNumberOfStudents())
                .build();
    }


    private void verifyAdminAccess() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found with email: " + email));
        if (!user.getRoles().contains(Roles.ADMIN)) {
            log.warn("Unauthorized access attempt by user: {}", email);
            throw new UnauthorizedException("Access restricted to ADMIN role");
        }
        if (user.getSchool() == null) {
            log.warn("User {} not associated with a school", email);
            throw new CustomNotFoundException("User not associated with a school");
        }
    }
}
