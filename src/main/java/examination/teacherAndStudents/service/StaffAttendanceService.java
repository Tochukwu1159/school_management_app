package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SubjectScheduleTeacherUpdateDto;
import examination.teacherAndStudents.entity.StaffAttendance;
import examination.teacherAndStudents.entity.SubjectSchedule;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface StaffAttendanceService {
    public void checkIn(String location);
    public void checkOut(String location);
    Page<StaffAttendance> getAllStaffAttendance(int pageNo, int pageSize, String sortBy);
//    double calculateAttendancePercentage(Long userId, StudentTerm term);
    Page<StaffAttendance> getStaffAttendanceByDateRange(LocalDate startDate, LocalDate endDate, int pageNo, int pageSize, String sortBy);
    List<StaffAttendance> getStaffAttendanceByStaffAndDateRange(
            String teacherId,
            LocalDate startDate,
            LocalDate endDate);

}
