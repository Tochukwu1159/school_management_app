package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.StudentTermService;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.TermStatus;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentTermServiceImpl implements StudentTermService {

    private final StudentTermRepository studentTermRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StudentTermDetailedResponse createStudentTerm(StudentTermRequest request) {
        // Validate admin access
        validateAdminAccess();

        // Validate dates
        validateTermDates(request.getStartDate(), request.getEndDate());

        AcademicSession session = academicSessionRepository.findById(request.getAcademicSessionId())
                .orElseThrow(() -> new NotFoundException(
                        "Academic session not found with ID: " + request.getAcademicSessionId()));

        // Check for duplicate term name in same session
        if (studentTermRepository.existsByNameAndAcademicSession(request.getName(), session)) {
            throw new EntityAlreadyExistException("Term with this name already exists in the selected academic session");
        }

        StudentTerm term = StudentTerm.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .resultReadyDate(request.getResultReadyDate())
                .academicSession(session)
                .termStatus(TermStatus.ACTIVE) // Default status
                .build();

        StudentTerm savedTerm = studentTermRepository.save(term);
        log.info("Created new student term: {}", savedTerm.getName());

        return mapToDetailedResponse(savedTerm);
    }

    @Override
    @Transactional
    public StudentTermDetailedResponse updateStudentTerm(Long id, StudentTermRequest request) {
        validateAdminAccess();

        StudentTerm term = studentTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student term not found with ID: " + id));

        // Prevent modification of active/completed terms
        if (term.getTermStatus() != TermStatus.COMPLETED) {
            throw new BadRequestException("Cannot modify term that is already  completed");
        }

        validateTermDates(request.getStartDate(), request.getEndDate());

        AcademicSession session = academicSessionRepository.findById(request.getAcademicSessionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic session not found with ID: " + request.getAcademicSessionId()));

        // Check for name conflict with other terms in same session
        if (studentTermRepository.existsByNameAndAcademicSessionAndIdNot(request.getName(), session, id)) {
            throw new EntityAlreadyExistException("Another term with this name already exists in the selected academic session");
        }

        term.setName(request.getName());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        term.setResultReadyDate(request.getResultReadyDate());
        term.setAcademicSession(session);

        StudentTerm updatedTerm = studentTermRepository.save(term);
        log.info("Updated student term with ID: {}", id);

        return mapToDetailedResponse(updatedTerm);
    }

    @Override
    @Transactional
    public void deleteStudentTerm(Long id) {
        validateAdminAccess();

        StudentTerm term = studentTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student term not found with ID: " + id));

        // Prevent deletion of active terms
        if (term.getTermStatus() == TermStatus.ACTIVE) {
            throw new BadRequestException("Cannot delete active term");
        }

        studentTermRepository.delete(term);
        log.info("Deleted student term with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentTermDetailedResponse getStudentTermById(Long id) {
        StudentTerm term = studentTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student term not found with ID: " + id));

        return mapToDetailedResponse(term);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentTermDetailedResponse> getAllStudentTerms(Pageable pageable) {
        return studentTermRepository.findAll(pageable)
                .map(this::mapToDetailedResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentTermDetailedResponse> getStudentTermsBySession(Long sessionId) {
        AcademicSession session = academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with ID: " + sessionId));

        return studentTermRepository.findByAcademicSession(session).stream()
                .map(this::mapToDetailedResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateTermStatus(Long id, TermStatus status) {
        validateAdminAccess();

        StudentTerm term = studentTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student term not found with ID: " + id));

        term.setTermStatus(status);
        studentTermRepository.save(term);
        log.info("Updated status for term ID {} to {}", id, status);
    }


    private void validateAdminAccess() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        userRepository.findByEmailAndRolesIn(email, Collections.singleton(Roles.ADMIN))
                .orElseThrow(() -> new UnauthorizedException("Admin privileges required"));
    }

    private void validateTermDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Term start date cannot be after end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Term cannot start in the past");
        }
    }


    private StudentTermDetailedResponse mapToDetailedResponse(StudentTerm term) {
        return StudentTermDetailedResponse.builder()
                .id(term.getId())
                .name(term.getName())
                .startDate(term.getStartDate())
                .endDate(term.getEndDate())
                .resultReadyDate(term.getResultReadyDate())
                .academicSession(mapToSessionResponse(term.getAcademicSession()))
                .status(term.getTermStatus())
                .createdDate(term.getCreatedDate())
                .updatedDate(term.getUpdatedDate())
                .build();
    }

    private AcademicSessionResponse mapToSessionResponse(AcademicSession session) {
        return AcademicSessionResponse.builder()
                .id(session.getId())
                .sessionName(session.getSessionName().getName())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .build();
    }
}