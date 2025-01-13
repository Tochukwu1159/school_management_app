package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Rating;
import examination.teacherAndStudents.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findBySchool(School school);
}
