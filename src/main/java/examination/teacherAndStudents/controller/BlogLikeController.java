package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.service.BlogLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/blog/likes")
@RequiredArgsConstructor
public class BlogLikeController {
    private final BlogLikeService blogLikeService;

    @PostMapping("/{blogId}")
    public ResponseEntity<ApiResponse<String>> toggleLike(@PathVariable Long blogId) {
        try {
            boolean isLiked = blogLikeService.toggleLikeBlog(blogId);
            String message = isLiked ? "Like added successfully" : "Like removed successfully";
            return ResponseEntity.ok(new ApiResponse<>(message, true, null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>("Error toggling like: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/{blogId}/count")
    public ResponseEntity<ApiResponse<Long>> getBlogLikeCount(@PathVariable Long blogId) {
        try {
            Long likeCount = blogLikeService.getBlogLikeCount(blogId);
            return ResponseEntity.ok(new ApiResponse<>("Like count retrieved successfully", true, likeCount));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>("Error retrieving like count: " + e.getMessage(), false, null));
        }
    }
}