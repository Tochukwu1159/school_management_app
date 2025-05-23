package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffAttendanceRepository extends JpaRepository<StaffAttendance, Long> {

    boolean existsByStaffAndDateAndAcademicYearAndStudentTerm(Profile staff, LocalDateTime date, AcademicSession academicYear, StudentTerm studentTerm);

    List<StaffAttendance> findByStaffAndAcademicYearAndStudentTerm(Profile staff, AcademicSession academicYear, StudentTerm studentTerm);

    List<StaffAttendance> findByStaffAndDateBetween(Profile staff, LocalDateTime startDate, LocalDateTime endDate);

    List<StaffAttendance> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<StaffAttendance> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<StaffAttendance> findByStaffUniqueRegistrationNumberAndDateBetween(String uniqueRegistrationNumber, LocalDateTime startDate, LocalDateTime endDate);

    Optional<TeacherAttendancePercent> findByStaffAndStudentTerm(Profile teacherProfile, StudentTerm studentTerm);
}