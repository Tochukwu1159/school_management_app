package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.BlogRequest;
import examination.teacherAndStudents.dto.BlogResponse;
import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<BlogResponse>> getAllBlogPosts() {
        try {
            List<BlogResponse> blogPosts = blogService.getAllBlogPosts();
            return ResponseEntity.ok(blogPosts);
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.status(500).build();
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