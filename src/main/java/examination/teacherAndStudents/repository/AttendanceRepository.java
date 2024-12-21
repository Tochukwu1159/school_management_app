package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Attendance;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import examination.teacherAndStudents.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByUserProfileAndDateBetween(Profile student, LocalDate startDate, LocalDate endDate);


    List<Attendance> findByUserProfileInAndDateBetween(Collection<Profile> userProfile, LocalDate date, LocalDate date2);

    long countByUserProfileIdAndStudentTerm(Long profileId, StudentTerm studentTerm);

    long countByUserProfileIdAndStudentTermAndStatus(Long profileId, StudentTerm studentTerm, AttendanceStatus attendanceStatus);


    Attendance findByUserProfileAndDateAndAcademicYearAndStudentTerm(Profile studentProfle, LocalDate date, AcademicSession academicSession, Optional<StudentTerm> studentTerm);
}
