package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.StaffLevel;
import examination.teacherAndStudents.entity.StoreItem;
import examination.teacherAndStudents.entity.StoreItemTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreItemTrackerRepository extends JpaRepository<StoreItemTracker, Long> {
    Optional<StoreItemTracker> findByStoreItemAndAcademicYear(StoreItem item, AcademicSession academicYear);
}