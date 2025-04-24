package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ComplaintDto;
import examination.teacherAndStudents.dto.ComplaintResponse;
import examination.teacherAndStudents.dto.ReplyComplaintDto;
import examination.teacherAndStudents.entity.Complaint;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.SubscriptionExpiredException;
import examination.teacherAndStudents.repository.ComplaintRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.ComplaintService;
import examination.teacherAndStudents.utils.ComplainStatus;
import examination.teacherAndStudents.utils.Roles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Validated
public class ComplaintServiceImpl implements ComplaintService {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintServiceImpl.class);

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<ComplaintResponse> getUserComplaints(Long userId, int page, int size, String sortBy, String sortDirection) {
        User currentUser = verifyUserAccess();
        logger.info("User {} fetching complaints for user ID: {}", currentUser.getEmail(), userId);

        if (!currentUser.getId().equals(userId) && !currentUser.getRoles().contains(Roles.ADMIN)) {
            logger.warn("User {} attempted to access complaints for another user ID: {}", currentUser.getEmail(), userId);
            throw new AuthenticationFailedException("Cannot access complaints of another user");
        }

        Profile userProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found for user ID: " + userId));

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Complaint> complaints = complaintRepository.findByComplainedBy(userProfile, pageable);
        logger.debug("Retrieved {} complaints for user ID: {}", complaints.getTotalElements(), userId);
        return complaints.map(complaint -> modelMapper.map(complaint, ComplaintResponse.class));
    }

    @Transactional
    @Override
    public ComplaintResponse submitComplaint(ComplaintDto complaintDto) {
        User user = verifyNonAdminAccess();
        logger.info("User {} submitting complaint", user.getEmail());

        validateSubscription(user.getSchool());

        Profile userProfile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomNotFoundException("Profile not found for user ID: " + user.getId()));

        Complaint complaint = Complaint.builder()
                .feedbackText(complaintDto.getFeedbackText())
                .complainStatus(ComplainStatus.OPEN)
                .complainedBy(userProfile)
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);
        logger.debug("Submitted complaint ID: {} for user ID: {}", savedComplaint.getId(), user.getId());
        return modelMapper.map(savedComplaint, ComplaintResponse.class);
    }

    @Transactional
    @Override
    public ComplaintResponse replyToComplaint(Long complaintId, ReplyComplaintDto replyComplaintDto) {
        User admin = verifyAdminAccess();
        logger.info("Admin {} replying to complaint ID: {}", admin.getEmail(), complaintId);

        validateSubscription(admin.getSchool());

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomNotFoundException("Complaint not found with ID: " + complaintId));

        Profile adminProfile = profileRepository.findByUserId(admin.getId())
                .orElseThrow(() -> new CustomNotFoundException("Profile not found for admin ID: " + admin.getId()));

        complaint.setReplyText(replyComplaintDto.getReplyText());
        complaint.setRepliedBy(adminProfile);
        complaint.setComplainStatus(ComplainStatus.CLOSED);
        complaint.setReplyTime(LocalDateTime.now());

        Complaint updatedComplaint = complaintRepository.save(complaint);
        logger.debug("Replied to complaint ID: {} by admin ID: {}", complaintId, admin.getId());
        return modelMapper.map(updatedComplaint, ComplaintResponse.class);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ComplaintResponse> getAllComplaint(
            int page,
            int size,
            String sortBy,
            String sortDirection,
            String userName,
            ComplainStatus status
    ) {
        User admin = verifyAdminAccess();
        logger.info("Admin {} fetching all complaints", admin.getEmail());

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Complaint> complaints = complaintRepository.findByFilters(
                admin.getSchool().getId(),
                userName,
                status,
                pageable
        );
        logger.debug("Retrieved {} complaints for school ID: {}", complaints.getTotalElements(), admin.getSchool().getId());
        return complaints.map(complaint -> modelMapper.map(complaint, ComplaintResponse.class));
    }

    private void validateSubscription(School school) {
        if (school == null || !school.isSubscriptionValid()) {
            logger.warn("Operation blocked for school due to expired subscription");
            throw new SubscriptionExpiredException("Active school subscription required");
        }
    }

    private User verifyUserAccess() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found with email: " + email));
    }

    private User verifyAdminAccess() {
        User user = verifyUserAccess();
        if (!user.getRoles().contains(Roles.ADMIN)) {
            logger.warn("Unauthorized access attempt by user: {}", user.getEmail());
            throw new AuthenticationFailedException("Access restricted to ADMIN role");
        }
        return user;
    }

    private User verifyNonAdminAccess() {
        User user = verifyUserAccess();
        if (user.getRoles().contains(Roles.ADMIN)) {
            logger.warn("Admin {} attempted to submit complaint", user.getEmail());
            throw new AuthenticationFailedException("Admins cannot submit complaints");
        }
        return user;
    }
}