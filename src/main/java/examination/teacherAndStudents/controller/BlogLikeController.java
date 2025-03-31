package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.service.BlogLikeService;
import examination.teacherAndStudents.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/blog/likes")
@RequiredArgsConstructor
public class BlogLikeController {
    private final BlogLikeService blogLikeService;

    @PostMapping("/{blogId}")
    public ResponseEntity<String> toggleLike(@PathVariable Long blogId) {
        blogLikeService.toggleLikeBlog(blogId);
        return ResponseEntity.ok("Like toggled successfully");
    }

    @GetMapping("/{blogId}/count")
    public ResponseEntity<Long> getBlogLikeCount(@PathVariable Long blogId) {
        Long likeCount = blogLikeService.getBlogLikeCount(blogId);
        return ResponseEntity.ok(likeCount);
    }
}
