package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.DuesRequest;
import examination.teacherAndStudents.dto.DuesResponse;
import examination.teacherAndStudents.entity.Dues;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DuesService {
    Page<DuesResponse> getAllDues(
            Long id,
            Long studentTermId,
            Long academicYearId,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    Dues getDuesById(Long id);
    Dues createDues(DuesRequest dues);
    Dues updateDues(Long id, DuesRequest updatedDues);
    boolean deleteDues(Long id);
}
