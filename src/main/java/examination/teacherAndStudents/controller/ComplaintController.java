package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ComplaintDto;
import examination.teacherAndStudents.dto.ComplaintResponse;
import examination.teacherAndStudents.dto.ReplyComplaintDto;
import examination.teacherAndStudents.service.ComplaintService;
import examination.teacherAndStudents.utils.ComplainStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);

    private final ComplaintService complaintService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<ComplaintResponse>> getUserComplaints(
            @PathVariable @Min(1) Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        logger.debug("Request to get complaints for user ID: {}, page: {}, size: {}", userId, page, size);
        Page<ComplaintResponse> complaints = complaintService.getUserComplaints(
                userId, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(complaints);
    }

    @PostMapping
    public ResponseEntity<ComplaintResponse> submitComplaint(@Valid @RequestBody ComplaintDto complaintDto) {
        logger.debug("Request to submit complaint with feedback: {}", complaintDto.getFeedbackText());
        ComplaintResponse submittedComplaint = complaintService.submitComplaint(complaintDto);
        return ResponseEntity.ok(submittedComplaint);
    }

    @PutMapping("/{complaintId}/reply")
    public ResponseEntity<ComplaintResponse> replyToComplaint(
            @PathVariable @Min(1) Long complaintId,
            @Valid @RequestBody ReplyComplaintDto replyComplaintDto
    ) {
        logger.debug("Request to reply to complaint ID: {}", complaintId);
        ComplaintResponse repliedComplaint = complaintService.replyToComplaint(complaintId, replyComplaintDto);
        return ResponseEntity.ok(repliedComplaint);
    }

    @GetMapping
    public ResponseEntity<Page<ComplaintResponse>> getAllComplaints(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) ComplainStatus status
    ) {
        logger.debug("Request to get all complaints, page: {}, size: {}, userName: {}, status: {}",
                page, size, userName, status);
        Page<ComplaintResponse> complaints = complaintService.getAllComplaint(
                page, size, sortBy, sortDirection, userName, status
        );
        return ResponseEntity.ok(complaints);
    }
}