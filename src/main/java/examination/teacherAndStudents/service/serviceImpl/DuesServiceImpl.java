package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.DuesRequest;
import examination.teacherAndStudents.dto.DuesResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.AcademicSessionRepository;
import examination.teacherAndStudents.repository.DuesRepository;
import examination.teacherAndStudents.repository.StudentTermRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.DuesService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DuesServiceImpl implements DuesService {

    private final DuesRepository duesRepository;
    private final UserRepository userRepository;
    private final StudentTermRepository studentTermRepository;
    private final AcademicSessionRepository academicSessionRepository;


    public Page<DuesResponse> getAllDues(
            Long id,
            Long studentTermId,
            Long academicYearId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

            // Create Pageable object
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Fetch filtered dues
            Page<Dues> duesPage = duesRepository.findAllBySchoolWithFilters(
                    admin.getSchool().getId(),
                    id,
                    studentTermId,
                    academicYearId,
                    pageable);

            // Map to response DTO
            return duesPage.map(this::mapToResponse);
        } catch (CustomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomInternalServerException("Error fetching dues: " + e.getMessage());
        }
    }

    private DuesResponse mapToResponse(Dues dues) {
        return DuesResponse.builder()
                .id(dues.getId())
                .purpose(dues.getPurpose())
                .amount(dues.getAmount())
                .studentTermId(dues.getStudentTerm() != null ? dues.getStudentTerm().getId() : null)
                .studentTermName(dues.getStudentTerm() != null ? dues.getStudentTerm().getName() : null)
                .academicYearId(dues.getAcademicYear().getId())
                .academicYearName(dues.getAcademicYear().getName())
                .build();
    }

    public Dues getDuesById(Long id) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        return duesRepository.findById(id).orElse(null);
    }

    @Transactional
    public Dues createDues(DuesRequest duesRequest) {
        // Authentication and authorization
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new UnauthorizedException("Admin privileges required"));

        // Validate input
        validateDuesRequest(duesRequest);

        // Fetch related entities
        StudentTerm studentTerm = studentTermRepository.findById(duesRequest.getStudentTerm())
                .orElseThrow(() -> new EntityNotFoundException("Student term not found"));

        AcademicSession academicSession = academicSessionRepository.findById(duesRequest.getAcademicYear())
                .orElseThrow(() -> new EntityNotFoundException("Academic session not found"));

        // Check for duplicate dues
        if (duesRepository.existsByPurposeAndStudentTermAndAcademicYear(
                duesRequest.getPurpose(),
                studentTerm,
                academicSession)) {
            throw new EntityAlreadyExistException("Dues entry already exists for this term and purpose");
        }

        // Create new dues
        Dues studentDues = Dues.builder()
                .studentTerm(studentTerm)
                .purpose(duesRequest.getPurpose())
                .amount(duesRequest.getAmount())
                .school(admin.getSchool()) // Use admin's school directly
                .academicYear(academicSession)
                .createdAt(LocalDateTime.now())
                .build();

        return duesRepository.save(studentDues);
    }

    private void validateDuesRequest(DuesRequest duesRequest) {
        if (duesRequest == null) {
            throw new IllegalArgumentException("Dues request cannot be null");
        }
        if (duesRequest.getPurpose() == null || duesRequest.getPurpose().trim().isEmpty()) {
            throw new IllegalArgumentException("Purpose cannot be empty");
        }
        if (duesRequest.getAmount() == null || duesRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (duesRequest.getStudentTerm() == null) {
            throw new IllegalArgumentException("Student term is required");
        }
        if (duesRequest.getAcademicYear() == null) {
            throw new IllegalArgumentException("Academic year is required");
        }
    }

    @Transactional
    public Dues updateDues(Long id, DuesRequest updatedDues) {
        // Authentication and authorization
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new UnauthorizedException("Admin privileges required"));

        // Validate input
        validateDuesRequest(updatedDues);

        // Fetch existing dues
        Dues existingDues = duesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dues not found"));

        // Verify school ownership
        if (!existingDues.getSchool().getId().equals(admin.getSchool().getId())) {
            throw new UnauthorizedException("Cannot modify dues from another school");
        }

        // Fetch related entities
        StudentTerm studentTerm = studentTermRepository.findById(updatedDues.getStudentTerm())
                .orElseThrow(() -> new EntityNotFoundException("Student term not found"));

        AcademicSession academicSession = academicSessionRepository.findById(updatedDues.getAcademicYear())
                .orElseThrow(() -> new EntityNotFoundException("Academic session not found"));

        // Check for duplicate dues (excluding current record)
        if (duesRepository.existsByPurposeAndStudentTermAndAcademicYearAndIdNot(
                updatedDues.getPurpose(),
                studentTerm,
                academicSession,
                id)) {
            throw new EntityAlreadyExistException("Another dues entry already exists for this term and purpose");
        }

        // Update dues
        existingDues.setPurpose(updatedDues.getPurpose());
        existingDues.setAmount(updatedDues.getAmount());
        existingDues.setAcademicYear(academicSession);
        existingDues.setStudentTerm(studentTerm);
        existingDues.setUpdatedAt(LocalDateTime.now());

        return duesRepository.save(existingDues);
    }

    @Transactional
    public boolean deleteDues(Long id) {
        // Authentication and authorization
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN)
                .orElseThrow(() -> new UnauthorizedException("Admin privileges required"));

        Dues existingDues = duesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dues not found"));

        // Verify school ownership
        if (!existingDues.getSchool().getId().equals(admin.getSchool().getId())) {
            throw new UnauthorizedException("Cannot delete dues from another school");
        }

        duesRepository.delete(existingDues);
        return false;
    }
}

