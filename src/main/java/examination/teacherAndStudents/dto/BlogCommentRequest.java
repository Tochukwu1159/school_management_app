package examination.teacherAndStudents.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogCommentRequest {
    @NotBlank(message = "Content cannot be blank")
    private String content;

    private Long parentCommentId; // For nested comments (optional)

}