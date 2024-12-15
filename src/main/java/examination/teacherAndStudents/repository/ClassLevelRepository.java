package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassLevelRepository extends JpaRepository<ClassLevel, Long> {
    ClassLevel findByClassName(String classAssigned);

}
