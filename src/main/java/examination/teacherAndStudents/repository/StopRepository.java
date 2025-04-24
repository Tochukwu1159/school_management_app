package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.BusRoute;
import examination.teacherAndStudents.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {
}