package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ComplaintDto;
import examination.teacherAndStudents.dto.ComplaintResponse;
import examination.teacherAndStudents.dto.ReplyComplaintDto;
import examination.teacherAndStudents.service.ComplaintService;
import examination.teacherAndStudents.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feedbacks")
public class ComplaintController {


    private final ComplaintService feedbackService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ComplaintResponse>> getUserComplaints(@PathVariable Long userId) {
        List<ComplaintResponse> feedbacks = feedbackService.getUserComplaints(userId);
        return ResponseEntity.ok(feedbacks);
    }

    @PostMapping
    public ResponseEntity<ComplaintResponse> submitComplaint(@RequestBody ComplaintDto feedback) {
        ComplaintResponse submittedComplaint = feedbackService.submitComplaint(feedback);
        return ResponseEntity.ok(submittedComplaint);
    }

    @PutMapping("/reply/feedbackId")
    public ResponseEntity<ComplaintResponse> replyToComplaint(@PathVariable  Long feedbackId, @RequestBody ReplyComplaintDto feedback) {
        ComplaintResponse submittedComplaint = feedbackService.replyToComplaint(feedbackId, feedback);
        return ResponseEntity.ok(submittedComplaint);
    }

    @GetMapping("/all")
        public ResponseEntity<Page<ComplaintResponse>> getAllComplaint(@RequestParam(defaultValue = AccountUtils.PAGENO) Integer pageNo,
                @RequestParam(defaultValue = AccountUtils.PAGESIZE) Integer pageSize,
                @RequestParam(defaultValue = "id") String sortBy) {
        Page<ComplaintResponse> feedbackList = feedbackService.getAllComplaint(pageNo, pageSize, sortBy);

        return ResponseEntity.ok(feedbackList);
    }
}
