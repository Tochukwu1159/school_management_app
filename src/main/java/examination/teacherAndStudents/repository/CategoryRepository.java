package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    int findByIdIn(Set<Long> categoryIds);
}