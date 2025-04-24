package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ComplaintDto;
import examination.teacherAndStudents.dto.ComplaintResponse;
import examination.teacherAndStudents.dto.ReplyComplaintDto;
import examination.teacherAndStudents.utils.ComplainStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ComplaintService {
    Page<ComplaintResponse> getUserComplaints(Long userId, int page, int size, String sortBy, String sortDirection) ;
    ComplaintResponse submitComplaint(ComplaintDto feedback);
    ComplaintResponse replyToComplaint(Long feedbackId, ReplyComplaintDto replyDto);
     Page<ComplaintResponse> getAllComplaint(
            int page,
            int size,
            String sortBy,
            String sortDirection,
            String userName,
            ComplainStatus status
    );}
