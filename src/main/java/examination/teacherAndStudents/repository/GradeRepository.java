package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Grade;
import examination.teacherAndStudents.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findBySchool(School school);
}