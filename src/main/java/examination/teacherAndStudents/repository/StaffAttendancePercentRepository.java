package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.StaffAttendancePercent;
import examination.teacherAndStudents.entity.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffAttendancePercentRepository extends JpaRepository<StaffAttendancePercent, Long> {
    Optional<StaffAttendancePercent> findByStaffAndStudentTerm(Profile teacherProfile, StudentTerm studentTerm);
}
