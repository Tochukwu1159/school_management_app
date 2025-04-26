package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.BlogRequest;
import examination.teacherAndStudents.dto.BlogResponse;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<BlogResponse>>> getAllBlogPosts(
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
                    title, schoolId, authorId, createdAtStart, createdAtEnd,
                    id, page, size, sortBy, sortDirection);
            return ResponseEntity.ok(new ApiResponse<>("Blog posts retrieved successfully", true, blogPosts));
        } catch (CustomInternalServerException e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(e.getMessage(), false, null));
        }
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<BlogResponse>> getBlogPostById(@PathVariable Long id) {
        try {
            BlogResponse blogPost = blogService.getBlogPostById(id);
            if (blogPost != null) {
                return ResponseEntity.ok(new ApiResponse<>("Blog post retrieved successfully", true, blogPost));
            } else {
                return ResponseEntity.status(404)
                        .body(new ApiResponse<>("Blog post not found", false, null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>("Error retrieving blog post: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<BlogResponse>> createBlogPost(@Valid @RequestBody BlogRequest blogRequest) {
        try {
            BlogResponse createdBlogPost = blogService.createBlogPost(blogRequest);
            return ResponseEntity.created(URI.create("/api/v1/blog/posts/" + createdBlogPost.getId()))
                    .body(new ApiResponse<>("Blog post created successfully", true, createdBlogPost));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>("Error creating blog post: " + e.getMessage(), false, null));
        }
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<BlogResponse>> updateBlogPost(@PathVariable Long id, @Valid @RequestBody BlogRequest updatedBlogRequest) {
        try {
            BlogResponse updatedPost = blogService.updateBlogPost(id, updatedBlogRequest);
            if (updatedPost != null) {
                return ResponseEntity.ok(new ApiResponse<>("Blog post updated successfully", true, updatedPost));
            } else {
                return ResponseEntity.status(404)
                        .body(new ApiResponse<>("Blog post not found", false, null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>("Error updating blog post: " + e.getMessage(), false, null));
        }
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBlogPost(@PathVariable Long id) {
        try {
            blogService.deleteBlogPost(id);
            return ResponseEntity.status(204)
                    .body(new ApiResponse<>("Blog post deleted successfully", true, null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>("Error deleting blog post: " + e.getMessage(), false, null));
        }
    }
}