package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.ComplaintDto;
import examination.teacherAndStudents.dto.ComplaintResponse;
import examination.teacherAndStudents.dto.ReplyComplaintDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ComplaintService {
    List<ComplaintResponse> getUserComplaints(Long userId);
    ComplaintResponse submitComplaint(ComplaintDto feedback);
    ComplaintResponse replyToComplaint(Long feedbackId, ReplyComplaintDto replyDto);
    Page<ComplaintResponse> getAllComplaint(int pageNo, int pageSize, String sortBy);
}
