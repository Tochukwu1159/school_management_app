package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.StaffAttendanceRequest;
import examination.teacherAndStudents.dto.StaffAttendanceResponse;
import examination.teacherAndStudents.entity.StaffAttendance;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface StaffAttendanceService {
    void takeStaffAttendance(StaffAttendanceRequest attendanceRequest);
    StaffAttendanceResponse calculateAttendancePercentage(Long staffId, Long sessionId, Long termId);
    List<StaffAttendanceResponse> calculateStaffAttendancePercentage(Long sessionId, Long termId, String role);
    List<StaffAttendance> getAllStaffAttendance();
    List<StaffAttendance> getStaffAttendanceByDateRange(LocalDate startDate, LocalDate endDate);
    List<StaffAttendance> getStaffAttendanceByStaffAndDateRange(Long staffId, LocalDate startDate, LocalDate endDate);
    Page<StaffAttendance> getAllStaffAttendance(int pageNo, int pageSize, String sortBy);
    Page<StaffAttendance> getStaffAttendanceByDateRange(LocalDate startDate, LocalDate endDate, int pageNo, int pageSize, String sortBy);
}