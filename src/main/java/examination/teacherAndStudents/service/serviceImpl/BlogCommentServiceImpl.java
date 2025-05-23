package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.BlogCommentRequest;
import examination.teacherAndStudents.dto.BlogCommentResponse;
import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.entity.BlogComment;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.BlogCommentRepository;
import examination.teacherAndStudents.repository.BlogRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.BlogCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogRepository blogRepository;
    private final ProfileRepository profileRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final UserRepository userRepository;

    // ===== ADD COMMENT (already done) =====
    public BlogCommentResponse addComment(Long blogId, BlogCommentRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));

        Blog blog = blogRepository.findByIdAndSchoolId(blogId,  user.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("Blog not found"));

        BlogComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = blogCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new CustomNotFoundException("Parent comment not found"));
        }

        BlogComment comment = BlogComment.builder()
                .blog(blog)
                .user(user.getUserProfile())
                .content(request.getContent())
                .parentComment(parentComment)
                .build();

        BlogComment savedComment = blogCommentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    // ===== REPLY TO COMMENT =====
    public BlogCommentResponse replyToComment(Long parentCommentId, BlogCommentRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Profile user = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));

        BlogComment parentComment = blogCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new CustomNotFoundException("Parent comment not found"));

        BlogComment reply = BlogComment.builder()
                .blog(parentComment.getBlog())
                .user(user)
                .content(request.getContent())
                .parentComment(parentComment)
                .build();

        BlogComment savedReply = blogCommentRepository.save(reply);
        return mapToResponse(savedReply);
    }

    // ===== GET BLOG COMMENTS (top-level) =====
    public List<BlogCommentResponse> getBlogComments(Long blogId) {
        List<BlogComment> comments = blogCommentRepository.findByBlogIdAndParentCommentIsNull(blogId);
        return comments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ===== GET COMMENT REPLIES =====
    public List<BlogCommentResponse> getCommentReplies(Long parentCommentId) {
        List<BlogComment> replies = blogCommentRepository.findByParentCommentId(parentCommentId);
        return replies.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ===== HELPER: ENTITY -> DTO MAPPING =====
    private BlogCommentResponse mapToResponse(BlogComment comment) {
        return BlogCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .authorName(comment.getUser().getUser().getFirstName() + " " + comment.getUser().getUser().getLastName()) // or .getEmail()
                .blogId(comment.getBlog().getId())
                .parentCommentId(comment.getParentComment() != null ?
                        comment.getParentComment().getId() : null)
                .replies(comment.getReplies() != null ?
                        comment.getReplies().stream()
                                .map(this::mapToResponse)
                                .toList() : Collections.emptyList())
                .build();
    }
}