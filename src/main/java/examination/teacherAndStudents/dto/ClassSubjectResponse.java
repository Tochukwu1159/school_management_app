package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSubjectResponse {
    private Long id;
    private SubjectResponse subject;
    private ClassBlockResponses classBlock;
    private AcademicSessionResponse academicYear;
    private SubjectUserResponse teacher;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

