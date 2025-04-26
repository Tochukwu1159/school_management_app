package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.BlogCommentRequest;
import examination.teacherAndStudents.dto.BlogCommentResponse;
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
    public ResponseEntity<ApiResponse<BlogCommentResponse>> addComment(@PathVariable Long blogId, @RequestBody BlogCommentRequest request) {
        BlogCommentResponse comment = blogCommentService.addComment(blogId, request);
        return ResponseEntity.ok(new ApiResponse<>("Comment added successfully", true, comment));
    }

    @PostMapping("/comments/{parentCommentId}/reply")
    public ResponseEntity<ApiResponse<BlogCommentResponse>> replyToComment(@PathVariable Long parentCommentId, @RequestBody BlogCommentRequest request) {
        BlogCommentResponse reply = blogCommentService.replyToComment(parentCommentId, request);
        return ResponseEntity.ok(new ApiResponse<>("Reply added successfully", true, reply));
    }

    @GetMapping("/{blogId}/comments")
    public ResponseEntity<ApiResponse<List<BlogCommentResponse>>> getBlogComments(@PathVariable Long blogId) {
        List<BlogCommentResponse> comments = blogCommentService.getBlogComments(blogId);
        return ResponseEntity.ok(new ApiResponse<>("Comments retrieved successfully", true, comments));
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<BlogCommentResponse>>> getCommentReplies(@PathVariable Long commentId) {
        List<BlogCommentResponse> replies = blogCommentService.getCommentReplies(commentId);
        return ResponseEntity.ok(new ApiResponse<>("Replies retrieved successfully", true, replies));
    }
}