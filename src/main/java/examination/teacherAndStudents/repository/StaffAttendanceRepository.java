package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StaffAttendance;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface StaffAttendanceRepository extends JpaRepository<StaffAttendance, Long> {

    List<StaffAttendance> findAllByStaffUniqueRegistrationNumberAndAndCheckInTimeBetween(String staffUniqueNumber, LocalDateTime startDate, LocalDateTime endDate);

    Page<StaffAttendance> findAllByCheckInTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);


    Optional<StaffAttendance> findFirstByStaffAndCheckInTimeBetweenOrderByCheckInTimeDesc(User teacher, LocalDateTime atStartOfDay, LocalDateTime atTime);

    Optional<StaffAttendance> findFirstByStaffOrderByCheckInTimeDesc(User teacher);
}
