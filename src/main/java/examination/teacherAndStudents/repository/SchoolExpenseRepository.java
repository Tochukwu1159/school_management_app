package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SchoolExpense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolExpenseRepository extends JpaRepository<SchoolExpense, Long> {
}
