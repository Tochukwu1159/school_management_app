package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.SickLeaveService;
import examination.teacherAndStudents.utils.Roles;
import examination.teacherAndStudents.utils.SickLeaveStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    public String applyForSickLeave(SickLeaveRequest request) {
        validateSickLeaveRequest(request);

        String email = getAuthenticatedUserEmail();
        Profile profile = getProfileByEmail(email);
        StudentTerm studentTerm = getStudentTerm(request.getTermId(), request.getSessionId());

        Leave sickLeave = buildSickLeaveEntity(request, profile, studentTerm.getAcademicSession(), studentTerm);
        sickLeaveRepository.save(sickLeave);

        log.info("Sick leave applied successfully [leaveId={}, email={}, startDate={}, endDate={}]",
                sickLeave.getId(), email, request.getStartDate(), request.getEndDate());

        return "Sick leave applied successfully.";
    }

    @Override
    @Transactional(readOnly = true)
    public List<Leave> getPendingSickLeaves() {
        return sickLeaveRepository.findByStatus(SickLeaveStatus.PENDING);
    }

    @Override
    @Transactional
    public String processSickLeaveRequest(Long sickLeaveId, SickLeaveRequestDto requestDto) {
        validateProcessRequest(requestDto);

        String email = getAuthenticatedUserEmail();
        Profile approvedBy = getProfileByEmail(email);
        Leave sickLeave = getSickLeaveById(sickLeaveId);

        validateSickLeaveForProcessing(sickLeave);

        updateSickLeaveStatus(sickLeave, requestDto, approvedBy);
        sickLeaveRepository.save(sickLeave);

        log.info("Sick leave processed [leaveId={}, action={}, email={}]",
                sickLeaveId, requestDto.getAction(), email);

        return String.format("Sick leave %sd successfully.", requestDto.getAction().toLowerCase());
    }

    @Override
    @Transactional
    public String cancelSickLeave(SickLeaveCancelRequest cancelRequest) {
        validateCancelRequest(cancelRequest);

        Leave sickLeave = getSickLeaveById(cancelRequest.getSickLeaveId());
        validateSickLeaveForCancellation(sickLeave);

        sickLeave.setCancelled(true);
        sickLeave.setCancelReason(cancelRequest.getCancelReason());
        sickLeaveRepository.save(sickLeave);

        log.info("Sick leave cancelled [leaveId={}, email={}]",
                cancelRequest.getSickLeaveId(), getAuthenticatedUserEmail());

        return "Sick leave cancelled successfully.";
    }

    @Override
    @Transactional(readOnly = true)
    public Leave getSickLeaveById(Long sickLeaveId) {
        return sickLeaveRepository.findById(sickLeaveId)
                .orElseThrow(() -> new NotFoundException("Sick leave not found with ID: " + sickLeaveId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Leave> getAllSickLeaves() {
        return sickLeaveRepository.findAll();
    }

    // Helper Methods
    private void validateSickLeaveRequest(SickLeaveRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new BadRequestException("Leave reason is required");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }
    }

    private String getAuthenticatedUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            log.error("Failed to retrieve authenticated user email", e);
            throw new CustomInternalServerException("Unable to authenticate user");
        }
    }

    private Profile getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found for user: " + email));
    }


    private StudentTerm getStudentTerm(Long termId, Long sessionId) {
        return studentTermRepository.findByIdAndAcademicSessionId(termId, sessionId)
                .orElseThrow(() -> new NotFoundException("Student term not found with ID: " + termId));
    }

    private Leave buildSickLeaveEntity(SickLeaveRequest request, Profile profile,
                                           AcademicSession academicSession, StudentTerm studentTerm) {
        int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (days <= 0) {
            throw new BadRequestException("End date must be after start date");
        }

        // Skip leave balance check for students
        if (!profile.getUser().getRoles().contains(Roles.STUDENT)) {
            if (profile.getRemainingLeaveDays() < days) {
                throw new InsufficientBalanceException("Insufficient leave days remaining");
            }
        }

        return Leave.builder()
                .appliedBy(profile)
                .academicYear(academicSession)
                .studentTerm(studentTerm)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .days(days)
                .reason(request.getReason())
                .status(SickLeaveStatus.PENDING)
                .build();
    }

    private void validateProcessRequest(SickLeaveRequestDto requestDto) {
        if (!"APPROVE".equalsIgnoreCase(requestDto.getAction()) &&
                !"REJECT".equalsIgnoreCase(requestDto.getAction())) {
            throw new BadRequestException("Invalid action. Must be 'APPROVE' or 'REJECT'");
        }
        if ("REJECT".equalsIgnoreCase(requestDto.getAction()) &&
                (requestDto.getReason() == null || requestDto.getReason().isBlank())) {
            throw new BadRequestException("Rejection reason is required");
        }
    }

    private void validateSickLeaveForProcessing(Leave sickLeave) {
        if (sickLeave.isCancelled()) {
            throw new IllegalStateException("Cannot process a cancelled sick leave request");
        }
        if (sickLeave.getStatus() != SickLeaveStatus.PENDING) {
            throw new IllegalStateException("Sick leave is not in pending status");
        }
    }

    private void updateSickLeaveStatus(Leave sickLeave, SickLeaveRequestDto requestDto, Profile approver) {
        if ("APPROVE".equalsIgnoreCase(requestDto.getAction())) {
            if (!sickLeave.getAppliedBy().getUser().getRoles().contains(Roles.STUDENT)) {
                int remainingDays = sickLeave.getAppliedBy().getRemainingLeaveDays();
                if (remainingDays < sickLeave.getDays()) {
                    throw new InsufficientBalanceException("Insufficient leave days remaining");
                }
                sickLeave.getAppliedBy().setRemainingLeaveDays(remainingDays - sickLeave.getDays());
            }
            sickLeave.setStatus(SickLeaveStatus.APPROVED);
        } else {
            sickLeave.setStatus(SickLeaveStatus.REJECTED);
            sickLeave.setRejectionReason(requestDto.getReason());
        }
        sickLeave.setRejectedBy(approver);
    }

    private void validateCancelRequest(SickLeaveCancelRequest request) {
        if (request.getCancelReason() == null || request.getCancelReason().isBlank()) {
            throw new BadRequestException("Cancel reason is required");
        }
    }

    private void validateSickLeaveForCancellation(Leave sickLeave) {
        if (sickLeave.isCancelled()) {
            throw new IllegalStateException("Sick leave is already cancelled");
        }
        if (sickLeave.getStatus() != SickLeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending sick leaves can be cancelled");
        }
    }
}