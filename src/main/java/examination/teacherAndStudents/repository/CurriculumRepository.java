package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

}