package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BlogRequest;
import examination.teacherAndStudents.dto.BlogResponse;
import org.springframework.data.domain.Page;


import java.time.LocalDateTime;

public interface BlogService {
    Page<BlogResponse> getAllBlogPosts(
            String title,
            Long schoolId,
            Long authorId,
            LocalDateTime createdAtStart,
            LocalDateTime createdAtEnd,
            Long id,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    BlogResponse getBlogPostById(Long id);
    BlogResponse createBlogPost(BlogRequest blogPost);
    BlogResponse updateBlogPost(Long id, BlogRequest updatedBlogPost);
    String deleteBlogPost(Long id);
}
