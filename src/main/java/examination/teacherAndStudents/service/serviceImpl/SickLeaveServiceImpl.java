package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SickLeaveRequest;
import examination.teacherAndStudents.entity.SickLeave;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.SickLeaveRepository;
import examination.teacherAndStudents.repository.UserRepository;
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


    public void applyForSickLeave(SickLeaveRequest sickLeaveRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> user = userRepository.findByEmail(email);

            if (user == null) {
                throw new CustomNotFoundException("Please log in as a user."); // Improved error message
            }
            SickLeave sickLeave = new SickLeave();
            sickLeave.setUser(user.get());
            sickLeave.setStartDate(sickLeaveRequest.getStartDate());
            sickLeave.setEndDate(sickLeaveRequest.getEndDate());
            sickLeave.setReason(sickLeaveRequest.getReason());
            sickLeave.setStatus(SickLeaveStatus.PENDING); // Initial status

            sickLeaveRepository.save(sickLeave);
        } catch (Exception e) {
            // Handle exceptions appropriately (log, rethrow, etc.)
            throw new RuntimeException("Error during leave application process.", e);
        }
    }

    public List<SickLeave> getPendingSickLeaves() {
        return sickLeaveRepository.findByStatus(SickLeaveStatus.PENDING);
    }


    public void updateSickLeave(Long sickLeaveId, SickLeaveRequest updatedSickLeave) {
        SickLeave existingSickLeave = getSickLeaveById(sickLeaveId);
        // Update fields based on the updatedSickLeave request
        existingSickLeave.setStartDate(updatedSickLeave.getStartDate());
        existingSickLeave.setEndDate(updatedSickLeave.getEndDate());
        existingSickLeave.setReason(updatedSickLeave.getReason());
        // Update other fields as needed
        sickLeaveRepository.save(existingSickLeave);
    }

    public void cancelSickLeave(Long sickLeaveId) {
        SickLeave sickLeave = getSickLeaveById(sickLeaveId);
        // Perform cancellation logic
        sickLeave.setCancelled(true);
        sickLeaveRepository.save(sickLeave);
    }

    public SickLeave getSickLeaveById(Long sickLeaveId) {
        return sickLeaveRepository.findById(sickLeaveId)
                .orElseThrow(() -> new RuntimeException("SickLeave not found with ID: " + sickLeaveId));
    }

    public List<SickLeave> getAllSickLeave() {
        return sickLeaveRepository.findAll();
    }}

