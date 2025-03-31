package examination.teacherAndStudents.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


// BlogCommentResponse.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String authorName; // User's name or email
    private Long blogId;
    private Long parentCommentId; // For nested comments
    private List<BlogCommentResponse> replies; // Nested replies
}