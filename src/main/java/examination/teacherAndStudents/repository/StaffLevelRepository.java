package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StaffLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffLevelRepository extends JpaRepository<StaffLevel, Long> {
    Optional<StaffLevel> findByIdAndSchoolId(Long staffLevelId, Long id);
}
