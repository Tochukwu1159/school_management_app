package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyMaterialRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String filePath;
    @NotNull
    private Long subjectId;
    @NotNull
    private Long teacherId;
    @NotNull
    private Long academicYearId;
    @NotNull
    private Long termId;
    @NotNull
    private Long classId;
}