package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.LessonPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonPlannerRepository extends JpaRepository<LessonPlan, Long> {
}