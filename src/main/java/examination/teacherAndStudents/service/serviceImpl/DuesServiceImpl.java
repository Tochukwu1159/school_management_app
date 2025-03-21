package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.DuesRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.DuesRepository;
import examination.teacherAndStudents.repository.StudentTermRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.DuesService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DuesServiceImpl implements DuesService {

    private final DuesRepository duesRepository;
    private final UserRepository userRepository;
    private final StudentTermRepository studentTermRepository;
    private final AcademicSessionRepository academicSessionRepository;


    public List<Dues> getAllDues() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }
        return duesRepository.findAll();
    }

    public Dues getDuesById(Long id) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }
        return duesRepository.findById(id).orElse(null);
    }

    public Dues createDues(DuesRequest duesRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();

        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }
        Optional<User> userDetails = userRepository.findByEmail(email);

        StudentTerm studentTerm = studentTermRepository.findById(duesRequest.getStudentTerm())
                .orElseThrow(() -> new NotFoundException("Student Term with id " + duesRequest.getStudentTerm() + " not found"));

        AcademicSession academicSession = academicSessionRepository.findById(duesRequest.getAcademicYear())
                .orElseThrow(() -> new NotFoundException("Academic Session with id " + duesRequest.getAcademicYear() + " not found"));

        // Validate duesRequest fields
        validateDuesRequest(duesRequest);

        Dues studentDues = new Dues();
        studentDues.setStudentTerm(studentTerm);
        studentDues.setPurpose(duesRequest.getPurpose());
        studentDues.setAmount(duesRequest.getAmount());
        studentDues.setSchool(userDetails.get().getSchool());
        studentDues.setAcademicYear(academicSession);
        return duesRepository.save(studentDues);
    }
    private void validateDuesRequest(DuesRequest duesRequest) {
        // Implement validation logic for duesRequest fields
        if (duesRequest.getAmount() == null || duesRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        // Add more validation as needed
    }

    public Dues updateDues(Long id, DuesRequest updatedDues) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }

        StudentTerm studentTerm = studentTermRepository.findById(updatedDues.getStudentTerm())
                .orElseThrow(() -> new NotFoundException("Student Term with id " + updatedDues.getStudentTerm() + " not found"));

        AcademicSession academicSession = academicSessionRepository.findById(updatedDues.getAcademicYear())
                .orElseThrow(() -> new NotFoundException("Academic Session with id " + updatedDues.getAcademicYear() + " not found"));

        // Validate updatedDues fields
        validateDuesRequest(updatedDues);
        Dues existingDues = duesRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Dues not found with id: " + id));

        existingDues.setPurpose(updatedDues.getPurpose());
        existingDues.setAmount(updatedDues.getAmount());
        existingDues.setAcademicYear(academicSession);
        existingDues.setStudentTerm(studentTerm);
        return duesRepository.save(existingDues);
    }
    public boolean deleteDues(Long id) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new AuthenticationFailedException("Please login as an Admin");
        }

        try {
            Dues existingDues = duesRepository.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Dues not found with id: " + id));

            duesRepository.delete(existingDues);
            return true;
        } catch (Exception e) {
            throw new CustomInternalServerException("An error occurred while deleting the dues entry"+ e.getMessage());
        }
    }
}

