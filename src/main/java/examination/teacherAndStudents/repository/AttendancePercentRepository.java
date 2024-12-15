package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AttendancePercent;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendancePercentRepository extends JpaRepository<AttendancePercent, Long> {
    Optional<AttendancePercent> findByUserAndStudentTerm(User teacher, StudentTerm term);
}
