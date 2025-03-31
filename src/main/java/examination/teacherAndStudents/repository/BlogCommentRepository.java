package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.entity.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {
    List<BlogComment> findByBlog(Blog blog);

    List<BlogComment> findByBlogIdAndParentCommentIsNull(Long blogId);

    List<BlogComment> findByParentCommentId(Long parentCommentId);
}
