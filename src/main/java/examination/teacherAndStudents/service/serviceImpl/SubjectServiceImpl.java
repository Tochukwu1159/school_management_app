package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SubjectRequest;
import examination.teacherAndStudents.dto.SubjectResponse;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.objectMapper.SubjectMapper;
import examination.teacherAndStudents.repository.ClassBlockRepository;
import examination.teacherAndStudents.repository.SubjectRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.SubjectService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ClassBlockRepository subClassRepository;

    private final SubjectMapper subjectMapper;
    private final ModelMapper modelMapper;


    public SubjectResponse createSubject(SubjectRequest subjectRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin"); // Return unauthorized response for non-admin users
            }

            ClassBlock subClass = subClassRepository.findById(subjectRequest.getSubClassId()).orElse(null);
            if (subClass == null) {
                throw new CustomNotFoundException("Class not found");
            }
            Subject subject = subjectMapper.mapToSubject(subjectRequest);
            subject.setName(subjectRequest.getName());
            subjectRepository.save(subject);
            return subjectMapper.mapToSubjectResponse(subject);

        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while creating the subject " + e.getMessage());
        }
    }

    public SubjectResponse updateSubject(Long subjectId, SubjectRequest updatedSubjectRequest) {
        try {
            // Check if the subject exists
            Subject existingSubject = subjectRepository.findById(subjectId).orElse(null);
            if (existingSubject == null) {
                throw new CustomNotFoundException("Subject not found");
            }

            // Check if the authenticated user is an admin
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            // Update the subject details
            existingSubject.setName(updatedSubjectRequest.getName());

            // Save the updated subject
            return modelMapper.map(subjectRepository.save(existingSubject), SubjectResponse.class);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while updating the subject " + e.getMessage());
        }
    }

    public SubjectResponse findSubjectById(Long subjectId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin"); // Return unauthorized response for non-admin users
            }

            return modelMapper.map(subjectRepository.findById(subjectId).orElse(null), SubjectResponse.class);
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while finding the subject " +  e.getMessage());
        }
    }

    public List<SubjectResponse> findAllSubjects() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin"); // Return unauthorized response for non-admin users
            }

            return subjectRepository.findAll()
                    .stream().map((element) -> modelMapper.map(element, SubjectResponse.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while fetching all subjects "+  e.getMessage());
        }
    }
}

