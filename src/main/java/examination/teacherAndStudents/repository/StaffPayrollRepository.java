package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StaffPayroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffPayrollRepository extends JpaRepository<StaffPayroll, Long> {
}
