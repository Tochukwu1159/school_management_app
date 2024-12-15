package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SubjectScheduleTeacherUpdateDto;
import examination.teacherAndStudents.dto.TeacherAttendanceRequest;
import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.entity.SubjectSchedule;
import examination.teacherAndStudents.entity.TeacherAttendance;
import examination.teacherAndStudents.utils.StudentTerm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TeacherAttendanceService {
    void takeTeacherAttendance(TeacherAttendanceRequest attendanceRequest);
    List<TeacherAttendance> getAllTeacherAttendance();
    double calculateAttendancePercentage(Long userId, StudentTerm term);
    List<TeacherAttendance> getTeacherAttendanceByDateRange(LocalDate startDate, LocalDate endDate);
    List<TeacherAttendance> getTeacherAttendanceByTeacherAndDateRange(
            Long teacherId,
            LocalDate startDate,
            LocalDate endDate);

    SubjectSchedule updateTeachingStatus(SubjectScheduleTeacherUpdateDto updateDto);


}
