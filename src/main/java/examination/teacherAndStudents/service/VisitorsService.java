package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.VisitorsRequest;
import examination.teacherAndStudents.dto.VisitorsResponse;
import org.springframework.data.domain.Page;

public interface VisitorsService {
    VisitorsResponse addVisitor(VisitorsRequest request);
    VisitorsResponse editVisitor(Long id, VisitorsRequest request);
    void deleteVisitor(Long id);
    Page<VisitorsResponse> getAllVisitors(int pageNo, int pageSize, String sortBy);
}
