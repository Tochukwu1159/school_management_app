package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.ClassSubject;
import examination.teacherAndStudents.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {

    boolean existsBySubjectAndClassBlockAndAcademicYear(Subject subject, ClassBlock classBlock, AcademicSession academicSession);
}
