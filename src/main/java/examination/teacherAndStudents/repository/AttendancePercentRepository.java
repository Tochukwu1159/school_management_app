package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AttendancePercent;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AttendancePercentRepository extends JpaRepository<AttendancePercent, Long> {
    Optional<AttendancePercent> findByUserAndStudentTerm(Profile teacher, StudentTerm term);

    Optional<AttendancePercent> findByUserAndSessionClassIdAndStudentTermId(Profile profile, Long id, Long id1);
}
