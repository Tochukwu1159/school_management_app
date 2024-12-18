package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HostelAllocationResponse {

    private Long id;  // Hostel Allocation ID
//    private User user;  // The student/user allocated to the hostel
//    private Hostel hostel;  // The hostel the student is allocated to
//    private int bedNumber;  // The bed number assigned to the student
//    private PaymentStatus paymentStatus;  // Payment status for the allocation
    private LocalDateTime datestamp;  // Date and time when the allocation was created
}
