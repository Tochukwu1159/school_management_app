package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BlogCommentRequest;
import examination.teacherAndStudents.dto.BlogCommentResponse;
import examination.teacherAndStudents.entity.BlogComment;

import java.util.List;

public interface BlogCommentService {
    BlogCommentResponse addComment(Long blogId, BlogCommentRequest request);
    BlogCommentResponse replyToComment(Long parentCommentId, BlogCommentRequest request);
    List<BlogCommentResponse> getBlogComments(Long blogId);
    List<BlogCommentResponse> getCommentReplies(Long parentCommentId);
}