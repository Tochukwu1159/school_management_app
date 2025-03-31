package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.StaffPayroll;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.utils.TermStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface StudentTermRepository extends JpaRepository<StudentTerm, Long> {
    StudentTerm findByNameAndAcademicSession(String name, AcademicSession academicSession);

    List<StudentTerm> findByResultReadyDate(LocalDate now);

    List<StudentTerm> findByAcademicSession(AcademicSession academicSession);

    List<StudentTerm> findByResultReadyDateAndTermStatus(LocalDate now, TermStatus termStatus);
}
