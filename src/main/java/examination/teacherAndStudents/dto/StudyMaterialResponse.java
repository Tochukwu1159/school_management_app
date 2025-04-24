package examination.teacherAndStudents.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyMaterialResponse {
    private Long id;
    private String title;
    private String filePath;
    private Long subjectId;
    private Long teacherId;
    private Long academicYearId;
    private Long termId;
    private Long classId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}