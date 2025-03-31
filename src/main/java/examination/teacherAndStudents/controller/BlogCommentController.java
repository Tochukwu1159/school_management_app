package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.BlogCommentRequest;
import examination.teacherAndStudents.dto.BlogCommentResponse;
import examination.teacherAndStudents.entity.BlogComment;
import examination.teacherAndStudents.service.BlogCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogCommentController {

    private final BlogCommentService blogCommentService;

    @PostMapping("/{blogId}/comment")
    public ResponseEntity<BlogCommentResponse> addComment(@PathVariable Long blogId, @RequestBody BlogCommentRequest request) {
        BlogCommentResponse comment = blogCommentService.addComment(blogId, request);
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/comments/{parentCommentId}/reply")
    public ResponseEntity<BlogCommentResponse> replyToComment(@PathVariable Long parentCommentId, @RequestBody BlogCommentRequest request) {
        BlogCommentResponse reply = blogCommentService.replyToComment(parentCommentId, request);
        return ResponseEntity.ok(reply);
    }

    @GetMapping("/{blogId}/comments")
    public ResponseEntity<List<BlogCommentResponse>> getBlogComments(@PathVariable Long blogId) {
        List<BlogCommentResponse> comments = blogCommentService.getBlogComments(blogId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<BlogCommentResponse>> getCommentReplies(@PathVariable Long commentId) {
        List<BlogCommentResponse> replies = blogCommentService.getCommentReplies(commentId);
        return ResponseEntity.ok(replies);
    }
}
