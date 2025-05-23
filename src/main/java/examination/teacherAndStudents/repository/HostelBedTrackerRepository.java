package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.Hostel;
import examination.teacherAndStudents.entity.HostelBedTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HostelBedTrackerRepository extends JpaRepository<HostelBedTracker, Long> {
    Optional<HostelBedTracker> findByHostelAndAcademicYear(Hostel hostel, AcademicSession academicYear);

    Optional<HostelBedTracker> findByHostelAndSchoolIdAndAcademicYear(Hostel hostel, Long id, AcademicSession academicYear);
}
