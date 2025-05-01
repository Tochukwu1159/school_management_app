package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.BusLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusLocationRepository extends JpaRepository<BusLocation, Long> {
    Optional<BusLocation> findByBusBusId(Long busId);}