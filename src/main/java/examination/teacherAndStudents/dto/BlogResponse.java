package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.Blog;
import examination.teacherAndStudents.entity.School;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogResponse {

    private Long id;
    private String title;
    private String imageUrl;
    private String content;
    private School school;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BlogResponse fromEntity(Blog blog) {
        BlogResponse response = new BlogResponse();
        response.setId(blog.getId());
        response.setTitle(blog.getTitle());
        response.setImageUrl(blog.getImageUrl());
        response.setContent(blog.getContent());
        response.setSchool(blog.getSchool());
        response.setCreatedAt(blog.getCreatedAt());
        response.setUpdatedAt(blog.getUpdatedAt());
        return response;
    }
}
