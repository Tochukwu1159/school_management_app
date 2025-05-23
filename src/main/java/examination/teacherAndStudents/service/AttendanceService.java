
package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.utils.AttendanceStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AttendanceService {
    void takeBulkAttendance(BulkAttendanceRequest request);
    Page<AttendanceResponses> getStudentAttendance(
            Long academicYearId,
            Long studentTermId,
            Long classBlockId,
            Long userProfileId,
            AttendanceStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection);
        List<Attendance> getStudentAttendanceByClass(Long classId, LocalDateTime startDate, LocalDateTime endDate);
    StudentAttendanceResponse calculateAttendancePercentage(Long userId, Long classBlockId, Long sessionId, Long studentTermId);
    List<StudentAttendanceResponse> calculateClassAttendancePercentage(Long classLevelId,Long sessionId, Long termId);
    Page<AttendanceResponses> getAllStudentsAttendance(
            Long studentId,
            Long academicYearId,
            Long studentTermId,
            Long classBlockId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime createdAt,
            int page,
            int size,
            String sortBy,
            String sortDirection);
}

