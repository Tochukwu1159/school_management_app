package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Rating;
import examination.teacherAndStudents.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findBySchool(School school);

    Optional<Rating> findBySchoolAndMinMarksAndMaxMarks(School school, int minMarks, int maxMarks);

    void deleteBySchool(School school);
}
