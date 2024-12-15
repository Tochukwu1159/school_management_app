package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AttendancePercent;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AttendancePercentRepository extends JpaRepository<AttendancePercent, Long> {
    Optional<AttendancePercent> findByUserAndStudentTerm(User teacher, StudentTerm term);
}
