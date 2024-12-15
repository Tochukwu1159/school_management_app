package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.BlogRequest;
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
    public ResponseEntity<List<Blog>> getAllBlogPosts() {
        try {
            List<Blog> blogPosts = blogService.getAllBlogPosts();
            return ResponseEntity.ok(blogPosts);
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Blog> getBlogPostById(@PathVariable Long id) {
        try {
            Blog blogPost = blogService.getBlogPostById(id);
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
    public ResponseEntity<Blog> createBlogPost(@RequestBody BlogRequest blogPost) {
        try {
            Blog createdBlogPost = blogService.createBlogPost(blogPost);
            return ResponseEntity.ok(createdBlogPost);
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Blog> updateBlogPost(@PathVariable Long id, @RequestBody BlogRequest updatedBlogPost) {
        try {
            Blog updatedPost = blogService.updateBlogPost(id, updatedBlogPost);
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
    public ResponseEntity<Void> deleteBlogPost(@PathVariable Long id) {
        try {
            boolean deleted = blogService.deleteBlogPost(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.status(500).build();
        }
    }
}