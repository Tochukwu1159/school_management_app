package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.BlogRequest;
import examination.teacherAndStudents.dto.BlogResponse;
import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/blog")
public class BlogController {

    private final BlogService blogService;

    @Autowired
    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<BlogResponse>> getAllBlogPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtEnd,
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        try {
            Page<BlogResponse> blogPosts = blogService.getAllBlogPosts(
                    title, schoolId, authorId,
                    createdAtStart, createdAtEnd,
                    id, page, size, sortBy, sortDirection);

            return ResponseEntity.ok(blogPosts);
        } catch (CustomInternalServerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/posts/{id}")
    public ResponseEntity<BlogResponse> getBlogPostById(@PathVariable Long id) {
        try {
            BlogResponse blogPost = blogService.getBlogPostById(id);
            if (blogPost != null) {
                return ResponseEntity.ok(blogPost);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/posts")
    public ResponseEntity<BlogResponse> createBlogPost(@RequestBody BlogRequest blogPost) {
        try {
            BlogResponse createdBlogPost = blogService.createBlogPost(blogPost);
            return ResponseEntity.ok(createdBlogPost);
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<BlogResponse> updateBlogPost(@PathVariable Long id, @RequestBody BlogRequest updatedBlogPost) {
        try {
            BlogResponse updatedPost = blogService.updateBlogPost(id, updatedBlogPost);
            if (updatedPost != null) {
                return ResponseEntity.ok(updatedPost);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<String> deleteBlogPost(@PathVariable Long id) {
        try {
            String deleted = blogService.deleteBlogPost(id);
                return ResponseEntity.ok(deleted);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}