package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.StaffPayroll;
import examination.teacherAndStudents.entity.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface StudentTermRepository extends JpaRepository<StudentTerm, Long> {
    StudentTerm findByNameAndAcademicSession(String name, AcademicSession academicSession);
}
