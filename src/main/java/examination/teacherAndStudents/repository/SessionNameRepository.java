package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SessionName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionNameRepository extends JpaRepository<SessionName, Long> {
    Optional<SessionName> findByName(String name);
}