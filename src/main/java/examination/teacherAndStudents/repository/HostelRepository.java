package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HostelRepository extends JpaRepository<Hostel, Long> {
    List<Hostel> findByAvailabilityStatus(AvailabilityStatus available);
}
