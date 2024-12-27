package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AcademicSessionRepository extends JpaRepository<AcademicSession, Long> {
    List<AcademicSession> findByEndDateBeforeAndStatus(LocalDate now, AcademicSession.Status status);
}