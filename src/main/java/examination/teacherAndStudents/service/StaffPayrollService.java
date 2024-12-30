package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StaffPayrollRequest;
import examination.teacherAndStudents.dto.StaffPayrollResponse;
import examination.teacherAndStudents.entity.StaffPayroll;

import java.util.List;

public interface StaffPayrollService {
    StaffPayrollResponse createOrUpdatePayroll(StaffPayrollRequest payrollRequest);
    StaffPayrollResponse getPayrollForStaff(Long staffId);
    List<StaffPayrollResponse> getPayrollForSchool();
    List<StaffPayrollResponse> getAllPayroll();
    List<StaffPayrollResponse> getPayrollForMonth(int month, int year);
    void deletePayroll(Long payrollId);
    String promoteStaff(Long userId, Long staffLevelId);
}
