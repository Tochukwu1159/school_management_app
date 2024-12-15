package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StaffPayroll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffPayrollRepository extends JpaRepository<StaffPayroll, Long> {
}
