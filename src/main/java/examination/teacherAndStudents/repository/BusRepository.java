package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByVehicleNumber(String busNumber);
}
