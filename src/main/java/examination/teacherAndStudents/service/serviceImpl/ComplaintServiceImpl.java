package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ComplaintDto;
import examination.teacherAndStudents.dto.ComplaintResponse;
import examination.teacherAndStudents.dto.ReplyComplaintDto;
import examination.teacherAndStudents.entity.Complaint;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.ComplaintRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.ComplaintService;
import examination.teacherAndStudents.utils.ComplainStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {



    private final ComplaintRepository complaintRepository;


    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ProfileRepository profileRepository;

    public List<ComplaintResponse> getUserComplaints(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Profile> userProfile = profileRepository.findByUser(user.get());
        return complaintRepository.findByComplainedBy(userProfile.get())
                .stream().map((element) -> modelMapper.map(element, ComplaintResponse.class))
                .collect(Collectors.toList());
    }

    public ComplaintResponse submitComplaint(ComplaintDto feedback) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> user = userRepository.findByEmail(email);
            Optional<Profile> userProfile = profileRepository.findByUser(user.get());
            Complaint newFeed = new Complaint();
            newFeed.setFeedbackText(feedback.getFeedbackText());
            newFeed.setComplainStatus(ComplainStatus.OPEN);
            newFeed.setComplainedBy(userProfile.get());
            return modelMapper.map(complaintRepository.save(newFeed), ComplaintResponse.class);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error sending feedback: " + e.getMessage());
        }
    }


    public ComplaintResponse replyToComplaint(Long feedbackId, ReplyComplaintDto replyComplaintDto) {
        try {
            // Find the feedback by ID
            Complaint feedback = complaintRepository.findById(feedbackId)
                    .orElseThrow(() -> new CustomNotFoundException("Complaint not found"));

            // Check if the authenticated user is an admin
            String adminEmail = SecurityConfig.getAuthenticatedUserEmail();
            User adminUser = userRepository.findByEmailAndRoles(adminEmail, Roles.ADMIN);
            if (adminUser == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }
            Optional<Profile> userProfile = profileRepository.findByUser(adminUser);
            // Set the reply text and update the submitted time
            feedback.setReplyText(replyComplaintDto.getReplyText());
            feedback.setRepliedBy(userProfile.get());
            feedback.setComplainStatus(ComplainStatus.CLOSED);

            // Save the updated feedback
            return modelMapper.map(complaintRepository.save(feedback), ComplaintResponse.class);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error replying to feedback: " + e.getMessage());
        }
    }

       public Page<ComplaintResponse> getAllComplaint(int pageNo, int pageSize, String sortBy){
            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        return complaintRepository.findAll(paging)
                .map((element) -> modelMapper.map(element, ComplaintResponse.class));
    }

}