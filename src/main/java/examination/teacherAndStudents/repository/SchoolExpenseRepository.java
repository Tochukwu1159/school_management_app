package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.SchoolExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolExpenseRepository extends JpaRepository<SchoolExpense, Long> {
}
