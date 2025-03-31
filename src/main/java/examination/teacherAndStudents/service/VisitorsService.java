package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.VisitorsRequest;
import examination.teacherAndStudents.dto.VisitorsResponse;
import examination.teacherAndStudents.utils.VisitorStatus;
import org.springframework.data.domain.Page;

public interface VisitorsService {
    VisitorsResponse addVisitor(VisitorsRequest request);
    VisitorsResponse editVisitor(Long id, VisitorsRequest request);
    void deleteVisitor(Long id);
    Page<VisitorsResponse> getAllVisitors(
            String name,
            String phoneNumber,
            String email,
            VisitorStatus status,
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDirection);
}
