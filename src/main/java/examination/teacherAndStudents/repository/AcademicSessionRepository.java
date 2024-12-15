package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicSessionRepository extends JpaRepository<AcademicSession, Long> {
}