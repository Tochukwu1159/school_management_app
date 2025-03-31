package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.entity.BlogLike;
import examination.teacherAndStudents.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogLikeRepository extends JpaRepository<BlogLike, Long> {

    Optional<BlogLike> findByBlogAndUser(Blog blog, Profile user);
}
