package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StaffLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffLevelRepository extends JpaRepository<StaffLevel, Long> {
}
