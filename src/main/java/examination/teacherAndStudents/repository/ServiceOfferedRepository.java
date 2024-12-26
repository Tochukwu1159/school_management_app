package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ServiceOffered;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOfferedRepository extends JpaRepository<ServiceOffered, Long> {
}
