package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.SickLeaveService;
import examination.teacherAndStudents.utils.SickLeaveStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SickLeaveServiceImpl implements SickLeaveService {

    private final SickLeaveRepository sickLeaveRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;

    @Override
    @Transactional
    public String applyForSickLeave(SickLeaveRequest sickLeaveRequest) {
        try {
            validateSickLeaveRequest(sickLeaveRequest);

            String email = SecurityConfig.getAuthenticatedUserEmail();
            Profile profile = getProfileByEmail(email);
            AcademicSession academicYear = getAcademicSession(sickLeaveRequest.getSessionId());
            StudentTerm studentTerm = getStudentTerm(sickLeaveRequest.getTermId());

            Leave leave = buildLeaveEntity(sickLeaveRequest, profile, academicYear, studentTerm);
            sickLeaveRepository.save(leave);

            log.info("Sick leave applied successfully by {} for dates {} to {}",
                    email, sickLeaveRequest.getStartDate(), sickLeaveRequest.getEndDate());

            return "Leave applied successfully.";
        } catch (IllegalArgumentException | NotFoundException e) {
            log.error("Validation error in leave application: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during leave application", e);
            throw new CustomInternalServerException("Error during leave application process "+ e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Leave> getPendingSickLeaves() {
        return sickLeaveRepository.findByStatus(SickLeaveStatus.PENDING);
    }

    @Override
    @Transactional
    public String processSickLeaveRequest(Long sickLeaveId, SickLeaveRequestDto requestDto) {
        try {
            validateProcessRequest(requestDto);

            String email = SecurityConfig.getAuthenticatedUserEmail();
            Profile approver = getProfileByEmail(email);
            Leave leave = getLeaveById(sickLeaveId).get();

            validateLeaveForProcessing(leave);

            updateLeaveStatus(leave, requestDto, approver);
            sickLeaveRepository.save(leave);

            log.info("Leave {} {} by {}", sickLeaveId, requestDto.getAction(), email);

            return String.format("Sick leave %sd successfully.", requestDto.getAction().toLowerCase());
        } catch (IllegalArgumentException | NotFoundException | IllegalStateException e) {
            log.error("Error processing leave request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing leave request", e);
            throw new CustomInternalServerException("Error processing leave request "+ e);
        }
    }

    @Override
    @Transactional
    public String cancelSickLeave(SickLeaveCancelRequest cancelRequest) {
        try {
            validateCancelRequest(cancelRequest);

            Leave leave = getLeaveById(cancelRequest.getSickLeaveId()).get();
            validateLeaveForCancellation(leave);

            leave.setCancelled(true);
            leave.setCancelReason(cancelRequest.getCancelReason());
            sickLeaveRepository.save(leave);

            log.info("Leave {} cancelled by {}", cancelRequest.getSickLeaveId(),
                    SecurityConfig.getAuthenticatedUserEmail());

            return "Leave cancelled successfully.";
        } catch (IllegalArgumentException | NotFoundException | IllegalStateException e) {
            log.error("Error cancelling leave: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error cancelling leave", e);
            throw new CustomInternalServerException("Error cancelling leave "+ e);
        }
    }

    private Optional<Leave> getLeaveById(Long sickLeaveId) {
        return sickLeaveRepository.findById(sickLeaveId);
    }

    @Override
    @Transactional(readOnly = true)
    public Leave getSickLeaveById(Long sickLeaveId) {
        return sickLeaveRepository.findById(sickLeaveId)
                .orElseThrow(() -> new NotFoundException("Leave not found with ID: " + sickLeaveId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Leave> getAllSickLeaves() {
        return sickLeaveRepository.findAll();
    }

    // Helper Methods
    private void validateSickLeaveRequest(SickLeaveRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("Leave reason is required");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
    }

    private Profile getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found for user: " + email));
    }

    private AcademicSession getAcademicSession(Long sessionId) {
        return academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Academic year not found with ID: " + sessionId));
    }

    private StudentTerm getStudentTerm(Long termId) {
        return studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found with ID: " + termId));
    }

    private Leave buildLeaveEntity(SickLeaveRequest request, Profile profile,
                                   AcademicSession academicYear, StudentTerm studentTerm) {
        return Leave.builder()
                .appliedBy(profile)
                .academicYear(academicYear)
                .studentTerm(studentTerm)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(SickLeaveStatus.PENDING)
                .build();
    }

    private void validateProcessRequest(SickLeaveRequestDto requestDto) {
        if (!"approve".equalsIgnoreCase(requestDto.getAction()) &&
                !"reject".equalsIgnoreCase(requestDto.getAction())) {
            throw new IllegalArgumentException("Invalid action. Must be 'approve' or 'reject'");
        }
        if ("reject".equalsIgnoreCase(requestDto.getAction()) &&
                (requestDto.getReason() == null || requestDto.getReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
    }

    private void validateLeaveForProcessing(Leave leave) {
        if (leave.getCancelled() != null && leave.getCancelled()) {
            throw new IllegalStateException("Cannot process a cancelled leave request");
        }
        if (leave.getStatus() != SickLeaveStatus.PENDING) {
            throw new IllegalStateException("Leave is not in pending status");
        }
    }

    private void updateLeaveStatus(Leave leave, SickLeaveRequestDto requestDto, Profile approver) {
        if ("approve".equalsIgnoreCase(requestDto.getAction())) {
            leave.setStatus(SickLeaveStatus.APPROVED);
        } else {
            leave.setStatus(SickLeaveStatus.REJECTED);
            leave.setRejectionReason(requestDto.getReason());
        }
        leave.setRejectedBy(approver);
    }

    private void validateCancelRequest(SickLeaveCancelRequest request) {
        if (request.getCancelReason() == null || request.getCancelReason().isBlank()) {
            throw new IllegalArgumentException("Cancel reason must be provided");
        }
    }

    private void validateLeaveForCancellation(Leave leave) {
        if (leave.getCancelled() != null && leave.getCancelled()) {
            throw new IllegalStateException("Leave is already cancelled");
        }
        if (leave.getStatus() != SickLeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leaves can be cancelled");
        }
    }
}