package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    Optional<Blog> findByIdAndSchool(Long id, School school);
}
