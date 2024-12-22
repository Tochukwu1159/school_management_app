package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SickLeaveCancelRequest;
import examination.teacherAndStudents.dto.SickLeaveRequest;
import examination.teacherAndStudents.dto.SickLeaveRequestDto;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.SickLeaveService;
import examination.teacherAndStudents.utils.SickLeaveStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SickLeaveServiceImpl implements SickLeaveService {

    private final SickLeaveRepository sickLeaveRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;


    public String applyForSickLeave(SickLeaveRequest sickLeaveRequest) {
        try {
            // Fetch authenticated user email
            String email = SecurityConfig.getAuthenticatedUserEmail();

            // Validate user existence
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

            // Validate academic year
            AcademicSession academicYear = academicSessionRepository.findById(sickLeaveRequest.getSessionId())
                    .orElseThrow(() -> new NotFoundException("Academic year not found with ID: " + sickLeaveRequest.getSessionId()));

            // Validate student term
            StudentTerm studentTerm = studentTermRepository.findById(sickLeaveRequest.getTermId())
                    .orElseThrow(() -> new NotFoundException("Term not found with ID: " + sickLeaveRequest.getTermId()));

            // Validate user profile
            Profile profile = profileRepository.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("Profile not found for user: " + email));

            // Validate leave request dates
            if (sickLeaveRequest.getStartDate().isAfter(sickLeaveRequest.getEndDate())) {
                throw new IllegalArgumentException("Start date cannot be after end date.");
            }

            // Validate reason for leave
            if (sickLeaveRequest.getReason() == null || sickLeaveRequest.getReason().isBlank()) {
                throw new IllegalArgumentException("Leave reason is required.");
            }

            // Create and save leave
            Leave leave = new Leave();
            leave.setAppliedBy(profile);
            leave.setAcademicYear(academicYear);
            leave.setStudentTerm(studentTerm);
            leave.setStartDate(sickLeaveRequest.getStartDate());
            leave.setEndDate(sickLeaveRequest.getEndDate());
            leave.setReason(sickLeaveRequest.getReason());
            leave.setStatus(SickLeaveStatus.PENDING); // Initial status

            sickLeaveRepository.save(leave);
            return "Leave applied successfully.";
        } catch (IllegalArgumentException | NotFoundException e) {
            // Handle specific validation errors
            throw e; // Re-throw the specific validation exception
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RuntimeException("Error during leave application process.", e);
        }
    }


    public List<Leave> getPendingSickLeaves() {

        return sickLeaveRepository.findByStatus(SickLeaveStatus.PENDING);

    }


    public String approveOrRejectSickLeaveRequest(Long sickLeaveId, SickLeaveRequestDto updatedSickLeave) {

        // Get authenticated user email
        String email = SecurityConfig.getAuthenticatedUserEmail();

        // Fetch user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        // Fetch profile for the authenticated user
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Profile not found for authenticated user"));

        // Fetch the sick leave by ID
        Leave existingLeave = sickLeaveRepository.findById(sickLeaveId)
                .orElseThrow(() -> new NotFoundException("Sick leave request not found with ID: " + sickLeaveId));

        // Check if the leave is cancelled
        if (existingLeave.getCancelled() != null && existingLeave.getCancelled()) {
            throw new IllegalStateException("Cannot approve or reject a cancelled leave request");
        }

        // Validate action and update status
        if ("approve".equalsIgnoreCase(updatedSickLeave.getAction())) {
            existingLeave.setStatus(SickLeaveStatus.APPROVED);
        } else if ("reject".equalsIgnoreCase(updatedSickLeave.getAction())) {
            existingLeave.setStatus(SickLeaveStatus.REJECTED);

            // Validate rejection reason
            if (updatedSickLeave.getReason() == null || updatedSickLeave.getReason().isBlank()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting a sick leave request");
            }
            existingLeave.setRejectionReason(updatedSickLeave.getReason());
        } else {
            throw new IllegalArgumentException("Invalid action. Must be 'approve' or 'reject'.");
        }
        existingLeave.setRejectedBy(profile);
        sickLeaveRepository.save(existingLeave);

        // Return success message
        return "Sick leave " + updatedSickLeave.getAction().toLowerCase() + "d successfully.";
    }

    public String cancelSickLeave(SickLeaveCancelRequest sickLeaveCancelRequest) {

        // Fetch the leave request by ID
        Leave leave = sickLeaveRepository.findById(sickLeaveCancelRequest.getSickLeaveId())
                .orElseThrow(() -> new NotFoundException("Leave not found with ID: " + sickLeaveCancelRequest.getSickLeaveId()));

        // Check if the leave is already cancelled
        if (leave.getCancelled() != null && leave.getCancelled()) {
            throw new IllegalStateException("The leave request is already cancelled");
        }

        // Check if the leave has been approved or rejected
        if (leave.getStatus() == SickLeaveStatus.APPROVED || leave.getStatus() == SickLeaveStatus.REJECTED) {
            throw new IllegalStateException("Cannot cancel a leave request that has already been " + leave.getStatus().toString().toLowerCase());
        }

        if (sickLeaveCancelRequest.getCancelReason() == null || sickLeaveCancelRequest.getCancelReason().isBlank()) {
            throw new IllegalArgumentException("Cancel reason must be provided");
        }

        // Update leave status to cancelled
        leave.setCancelled(true);
        leave.setCancelReason(sickLeaveCancelRequest.getCancelReason());
        sickLeaveRepository.save(leave);

        // Return success message
        return "Leave cancelled successfully.";
    }



    public Leave getSickLeaveById(Long sickLeaveId) {
        return sickLeaveRepository.findById(sickLeaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found with ID: " + sickLeaveId));
    }


    public List<Leave> getAllSickLeave() {
        return sickLeaveRepository.findAll();
    }}

