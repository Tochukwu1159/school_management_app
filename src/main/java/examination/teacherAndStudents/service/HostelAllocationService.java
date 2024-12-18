package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.HostelAllocationRequest;
import examination.teacherAndStudents.dto.HostelAllocationResponse;
import examination.teacherAndStudents.entity.HostelAllocation;
import examination.teacherAndStudents.utils.PaymentStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface HostelAllocationService {
    HostelAllocationResponse allocateStudentToHostel(HostelAllocationRequest request);
    HostelAllocationResponse payHotelAllocation(Long dueId, Long sessionId);
    List<HostelAllocationResponse> getAllHostelAllocations();
    Optional<HostelAllocationResponse> getHostelAllocationById(Long id);
    void deleteHostelAllocation(Long id);
    HostelAllocationResponse updatePaymentStatus(Long id, PaymentStatus paymentStatus);
}
