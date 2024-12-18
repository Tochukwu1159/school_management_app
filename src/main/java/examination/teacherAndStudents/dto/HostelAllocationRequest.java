package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.Data;

@Data
public class HostelAllocationRequest {

    private Long userId;  // Student/User ID
    private Long hostelId;  // Hostel ID
    private int bedNumber;  // Bed number within the hostel
    private Long allocationId;  // Payment status of the allocation
    private Long academicYearId;
}
