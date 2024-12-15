package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassSubject;
import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.utils.StudentTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {

    Optional<ClassSubject> findByIdAndTerm(Long subjectId, StudentTerm term);
}
