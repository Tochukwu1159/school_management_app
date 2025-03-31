package examination.teacherAndStudents.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String rackNo;
    private Integer quantityAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}