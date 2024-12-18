package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Transport;
import examination.teacherAndStudents.entity.TransportTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TransportTrackerRepository extends JpaRepository<TransportTracker, Long> {
    Optional<TransportTracker> findByTransport(Transport transport);
}