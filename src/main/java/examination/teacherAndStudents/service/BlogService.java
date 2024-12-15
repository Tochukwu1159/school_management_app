package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BlogRequest;
import examination.teacherAndStudents.entity.Blog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface BlogService {
    List<Blog> getAllBlogPosts();
    Blog getBlogPostById(Long id);
    Blog createBlogPost(BlogRequest blogPost);
    Blog updateBlogPost(Long id, BlogRequest updatedBlogPost);
    boolean deleteBlogPost(Long id);
}
