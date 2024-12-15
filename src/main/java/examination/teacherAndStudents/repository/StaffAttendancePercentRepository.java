package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StaffAttendancePercent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffAttendancePercentRepository extends JpaRepository<StaffAttendancePercent, Long> {
//    Optional<TeacherAttendancePercent> findByUserAndStudentTerm(User teacher, StudentTerm term);
}
