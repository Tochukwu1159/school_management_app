package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.AllocationStatus;
import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostelAllocationResponse {
    private Long id;
    private Long hostelId;
    private String hostelName;
    private Integer bedNumber;
    private Long profileId;
    private String studentName;
    private PaymentStatus paymentStatus;
    private AllocationStatus allocationStatus;
    private Long academicYearId;
    private Long feeId;
    private LocalDateTime datestamp;
}