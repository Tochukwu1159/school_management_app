package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.AllocationStatus;
import examination.teacherAndStudents.utils.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportAllocationResponse {
    private Long allocationId;
    private Long studentId;
    private String studentName;
    private Long routeId;
    private String routeName;
    private Long stopId;
    private String stopName;
    private Long transportId;
    private String transportName;
    private PaymentStatus paymentStatus;
    private AllocationStatus allocationStatus;
    private LocalDateTime createdDate;
}