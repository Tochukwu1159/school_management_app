package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StaffMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffMovementRepository extends JpaRepository<StaffMovement, Long> {
}
