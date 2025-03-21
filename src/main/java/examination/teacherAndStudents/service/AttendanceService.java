
package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.AttendanceRequest;
import examination.teacherAndStudents.dto.AttendanceResponse;
import examination.teacherAndStudents.dto.StudentAttendanceResponse;
import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.entity.StudentTerm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AttendanceService {
    void takeAttendance(AttendanceRequest attendanceRequest);
    AttendanceResponse getStudentAttendance(Long studentId, LocalDateTime startDate, LocalDateTime endDate);
        List<Attendance> getStudentAttendanceByClass(Long classId, LocalDateTime startDate, LocalDateTime endDate);
    StudentAttendanceResponse calculateAttendancePercentage(Long userId, Long classLevelId, Long sessionId, Long studentTermId);
    List<StudentAttendanceResponse> calculateClassAttendancePercentage(Long classLevelId,Long sessionId, Long termId);
}

