package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StaffRequest;
import examination.teacherAndStudents.dto.StaffResponse;
import org.springframework.data.domain.Page;

public interface StaffService {
    StaffResponse createStaff(StaffRequest staffRequest);
    StaffResponse updateStaff(Long staffId, StaffRequest updatedStaff);
    Page<StaffResponse> findAllStaff(String searchTerm, int page, int size, String sortBy);
    StaffResponse findStaffById(Long StaffId);
    StaffResponse deactivateStaff(String uniqueRegistrationNumber);
    void deleteStaff(Long StaffId);
}
