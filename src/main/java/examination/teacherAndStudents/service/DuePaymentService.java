package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.DuePaymentRequest;
import examination.teacherAndStudents.dto.DuePaymentResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface DuePaymentService {
    DuePaymentResponse makeDuePayment(DuePaymentRequest duePaymentRequest);
    Page<DuePaymentResponse> getAllDuePayments(
            Long id,
            Long studentTermId,
            Long academicYearId,
            Long profileId,
            Long dueId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    DuePaymentResponse getDuePaymentById(Long id);
    Page<DuePaymentResponse> getAllDuePaymentsByUser(
            Long userId,
            Long dueId,
            Long studentTermId,
            Long academicYearId,
            LocalDateTime createdAt,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    void deleteDuePaymentById(Long id);

}
