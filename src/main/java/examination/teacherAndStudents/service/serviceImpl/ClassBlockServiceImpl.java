package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.ClassBlockRequest;
import examination.teacherAndStudents.dto.ClassBlockResponse;
import examination.teacherAndStudents.dto.UpdateFormTeacherRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.ClassBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassBlockServiceImpl implements ClassBlockService {

    private final ClassBlockRepository classBlockRepository;
    private final ClassLevelRepository classLevelRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AcademicSessionRepository academicSessionRepository;

    public ClassBlockResponse createClassBlock(ClassBlockRequest request) {
        try {
            // Fetch the class level by ID
            ClassLevel classLevel = classLevelRepository.findById(request.getClassLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class Level not found with ID: " + request.getClassLevelId()));



            // Build the ClassBlock entity
            ClassBlock classBlock = ClassBlock.builder()
                    .classLevel(classLevel)
                    .name(request.getSubClassName())
                    .classUniqueUrl(request.getClassUniqueUrl())
                    .numberOfStudents(0)
                    .build();

            // Save the ClassBlock entity
            classBlockRepository.save(classBlock);

            // Map and return the response DTO
            return mapToResponse(classBlock);
        } catch (ResourceNotFoundException e) {
            // Rethrow custom exceptions for clarity
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            throw new RuntimeException("An error occurred while creating the class block: " + e.getMessage());
        }
    }


    public ClassBlockResponse getClassBlockById(Long id) {
        try {
            // Fetch the class block by ID
            ClassBlock classBlock = classBlockRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Class Block not found with ID: " + id));

            // Map the entity to a response DTO and return
            return mapToResponse(classBlock);
        } catch (ResourceNotFoundException e) {
            // Rethrow custom exceptions for clarity
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            throw new RuntimeException("An error occurred while retrieving the class block: " + e.getMessage());
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
                    subClassId,
                    academicYearId);

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
            // Fetch the class block by ID
            ClassBlock classBlock = classBlockRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Class Block not found"));

            // Update the class block details
            classBlock.setClassUniqueUrl(request.getClassUniqueUrl());

            // Save the updated class block
            classBlockRepository.save(classBlock);

            // Return the updated class block as a response
            return mapToResponse(classBlock);
        } catch (ResourceNotFoundException e) {
            // Handle the specific exception if the class block is not found
            throw new RuntimeException("Resource not found: " + e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected exceptions
            throw new RuntimeException("An unexpected error occurred while updating the class block: " + e.getMessage());
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
            throw new RuntimeException("Resource not found: " + e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected exceptions
            throw new RuntimeException("An unexpected error occurred while deleting the class block: " + e.getMessage());
        }
    }


    public ClassBlockResponse updateFormTeacher(UpdateFormTeacherRequest request) {
        try {
            AcademicSession session = academicSessionRepository.findById(request.getSessionId())
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


    public ClassBlockResponse changeStudentClass(Long studentId, ClassBlockRequest request) {
        try {
            // Fetch the student user entity
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            // Fetch the student's profile
            Profile studentProfile = profileRepository.findByUser(student)
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

            // Fetch the current class block (the student's existing class)
            ClassBlock currentClass = studentProfile.getClassBlock();
            if (currentClass == null || !currentClass.getId().equals(request.getClassLevelId())) {
                throw new IllegalArgumentException("Student is not currently in the specified class block.");
            }

            // Fetch the new class block (the destination class)
            ClassBlock newClass = classBlockRepository.findById(request.getNextClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("New class block not found"));

            // Update the student counts for the classes
            synchronized (this) {
                currentClass.setNumberOfStudents(currentClass.getNumberOfStudents() - 1);
                newClass.setNumberOfStudents(newClass.getNumberOfStudents() + 1);
                classBlockRepository.save(currentClass);
                classBlockRepository.save(newClass);
            }

            // Update the student's class block
            studentProfile.setClassBlock(newClass);
            profileRepository.save(studentProfile);

            // Return the response
            return mapToResponse(newClass);
        } catch (ResourceNotFoundException e) {
            // Handle specific not found exceptions
            throw new RuntimeException("Resource not found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            throw new RuntimeException("Validation error: " + e.getMessage());
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage());
        }
    }

    private ClassBlockResponse mapToResponse(ClassBlock classBlock) {
        return ClassBlockResponse.builder()
                .id(classBlock.getId())
                .name(classBlock.getName())
                .classLevelId(classBlock.getClassLevel().getId())
                .classUniqueUrl(classBlock.getClassUniqueUrl())
                .numberOfStudents(classBlock.getNumberOfStudents())
                .build();
    }
}
