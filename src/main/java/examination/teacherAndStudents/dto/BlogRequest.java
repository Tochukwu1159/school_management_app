package examination.teacherAndStudents.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class BlogRequest {
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

}
