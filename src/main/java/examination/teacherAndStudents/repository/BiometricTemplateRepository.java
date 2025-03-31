package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.BiometricTemplate;
import examination.teacherAndStudents.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BiometricTemplateRepository extends JpaRepository<BiometricTemplate, Long> {
    Optional<BiometricTemplate> findByStaff(Profile staff);
}
