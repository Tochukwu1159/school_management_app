package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SubjectScheduleTeacherUpdateDto;
import examination.teacherAndStudents.dto.TeacherAttendanceRequest;
import examination.teacherAndStudents.dto.TeacherAttendanceResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.TeacherAttendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TeacherAttendanceService {
    void takeTeacherAttendance(TeacherAttendanceRequest attendanceRequest);
    List<TeacherAttendance> getAllTeacherAttendance();
    TeacherAttendanceResponse calculateAttendancePercentage(Long userId,Long sessionId, Long term);
    List<TeacherAttendance> getTeacherAttendanceByDateRange(LocalDate startDate, LocalDate endDate);
    List<TeacherAttendance> getTeacherAttendanceByTeacherAndDateRange(
            Long teacherId,
            LocalDate startDate,
            LocalDate endDate);

    SubjectSchedule updateTeachingStatus(SubjectScheduleTeacherUpdateDto updateDto);
    List<TeacherAttendanceResponse> calculateTeacherAttendancePercentage(Long sessionId, Long termId);


}
