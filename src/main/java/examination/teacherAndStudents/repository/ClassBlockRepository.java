package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.ClassLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassBlockRepository extends JpaRepository<ClassBlock, Long> {

}