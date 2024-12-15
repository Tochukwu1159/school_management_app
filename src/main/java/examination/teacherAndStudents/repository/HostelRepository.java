package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface HostelRepository extends JpaRepository<Hostel, Long> {
    List<Hostel> findByAvailabilityStatus(AvailabilityStatus available);
}
