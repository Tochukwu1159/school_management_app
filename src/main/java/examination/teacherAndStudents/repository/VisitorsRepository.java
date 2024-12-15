package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Visitors;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VisitorsRepository extends JpaRepository<Visitors, Long> {
}
