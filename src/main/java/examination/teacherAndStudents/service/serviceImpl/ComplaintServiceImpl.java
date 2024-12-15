package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.ComplaintDto;
import examination.teacherAndStudents.dto.ComplaintResponse;
import examination.teacherAndStudents.dto.ReplyComplaintDto;
import examination.teacherAndStudents.entity.Complaint;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.ComplaintRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.ComplaintService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {



    private final ComplaintRepository complaintRepository;


    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<ComplaintResponse> getUserComplaints(Long userId) {
        return complaintRepository.findByUserId(userId)
                .stream().map((element) -> modelMapper.map(element, ComplaintResponse.class))
                .collect(Collectors.toList());
    }

    public ComplaintResponse submitComplaint(ComplaintDto feedback) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User user = userRepository.findByEmailAndRoles(email, Roles.STUDENT);
            if (user == null) {
                throw new CustomNotFoundException("Please login as a Student"); // Return unauthorized response for non-admin users
            }
            Complaint newFeed = new Complaint();
            newFeed.setReplyText(feedback.getFeedbackText());
            newFeed.setSubmittedTime(LocalDateTime.now());
            newFeed.setUser(user);
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

            // Set the reply text and update the submitted time
            feedback.setReplyText(replyComplaintDto.getReplyText());
            feedback.setReplyTime(LocalDateTime.now());

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