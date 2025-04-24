package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Grade;
import examination.teacherAndStudents.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findBySchool(School school);

    Optional<Grade> findBySchoolAndMinMarksAndMaxMarks(School school, int minMarks, int maxMarks);

    void deleteBySchool(School school);
}