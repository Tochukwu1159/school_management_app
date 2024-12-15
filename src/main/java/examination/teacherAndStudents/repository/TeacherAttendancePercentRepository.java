package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.TeacherAttendance;
import examination.teacherAndStudents.entity.TeacherAttendancePercent;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface TeacherAttendancePercentRepository extends JpaRepository<TeacherAttendancePercent, Long> {
    Optional<TeacherAttendancePercent> findByTeacherAndStudentTerm(Profile teacher, StudentTerm term);
}
