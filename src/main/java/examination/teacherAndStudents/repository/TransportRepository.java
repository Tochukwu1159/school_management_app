package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportRepository extends JpaRepository<Transport, Long> {
}
