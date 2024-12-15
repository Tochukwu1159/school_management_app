package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.entity.AttendancePercent;
import examination.teacherAndStudents.entity.TeacherAttendance;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.AttendanceStatus;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface TeacherAttendanceRepository extends JpaRepository<TeacherAttendance, Long> {

    List<TeacherAttendance> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<TeacherAttendance> findByTeacherIdAndDateBetween(Long teacherId, LocalDateTime startDateTime, LocalDateTime endDateTime);
    long countByTeacherIdAndTermAndAndStatus(long teacherId, StudentTerm studentTerm, AttendanceStatus attendanceStatus);

    long countByTeacherIdAndAndTerm(long teacherId, StudentTerm term);

    TeacherAttendance findByTeacherAndDate(User teacher, LocalDateTime attendanceDate);
}
