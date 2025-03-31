package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BlogRequest;
import examination.teacherAndStudents.dto.BlogResponse;
import examination.teacherAndStudents.entity.Blog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface BlogService {
    List<BlogResponse> getAllBlogPosts();
    BlogResponse getBlogPostById(Long id);
    BlogResponse createBlogPost(BlogRequest blogPost);
    BlogResponse updateBlogPost(Long id, BlogRequest updatedBlogPost);
    String deleteBlogPost(Long id);
}
