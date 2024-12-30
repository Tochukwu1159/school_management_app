package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StaffLevelRequest;
import examination.teacherAndStudents.dto.StaffLevelResponse;

import java.util.List;

public interface StaffLevelService {
    StaffLevelResponse createStaffLevel(StaffLevelRequest request);

    StaffLevelResponse editStaffLevel(Long id, StaffLevelRequest request);

    void deleteStaffLevel(Long id);

    List<StaffLevelResponse> getAllStaffLevels();

    StaffLevelResponse getStaffLevelById(Long id);
}
