package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StaffAttendancePercent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffAttendancePercentRepository extends JpaRepository<StaffAttendancePercent, Long> {
}
