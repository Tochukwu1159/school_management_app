package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.entity.AttendancePercent;
import examination.teacherAndStudents.entity.TeacherAttendance;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.utils.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface TeacherAttendanceRepository extends JpaRepository<TeacherAttendance, Long> {

    List<TeacherAttendance> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<TeacherAttendance> findByTeacherIdAndDateBetween(Long teacherId, LocalDateTime startDateTime, LocalDateTime endDateTime);
    long countByTeacherIdAndStudentTermAndAndStatus(Long teacherId, StudentTerm studentTerm, AttendanceStatus attendanceStatus);

    long countByTeacherIdAndAcademicYearAndStudentTerm(long teacherId, AcademicSession academicSession, StudentTerm term);

    TeacherAttendance findByTeacherAndDate(Profile teacher, LocalDateTime attendanceDate);


    TeacherAttendance findByTeacherAndDateAndAcademicYearAndStudentTerm(Profile profile, LocalDateTime attendanceDate, AcademicSession session, StudentTerm studentTerm);

    long countByTeacherIdAndStudentTerm(Long id, StudentTerm studentTerm);

    List<TeacherAttendance> findByTeacherIdAndAcademicYearAndStudentTerm(Long id, AcademicSession academicSession, StudentTerm studentTerm);

    boolean existsByTeacherAndDateAndAcademicYearAndStudentTerm(Profile teacherProfile, LocalDateTime attendanceDate, AcademicSession session, StudentTerm studentTerm);
}
