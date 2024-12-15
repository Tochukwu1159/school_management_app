package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.AttendanceStatus;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Attendance findByUserProfileAndDate(Profile student, LocalDate date);

    List<Attendance> findByUserProfileAndDateBetween(Profile student, LocalDate startDate, LocalDate endDate);


    List<Attendance> findByUserProfileInAndDateBetween(Collection<Profile> userProfile, LocalDate date, LocalDate date2);

    long countByUserProfileIdAndTerm(Long profileId, StudentTerm studentTerm);

    long countByUserProfileIdAndTermAndStatus(Long profileId, StudentTerm studentTerm, AttendanceStatus attendanceStatus);
}
