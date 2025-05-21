package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassBlockRequest {
    private Long sessionId;
    private Long currentClassBlockId;
    private Long newClassBlockId;
    private Long formTeacherId;
    private String subClassName;
    private String classUniqueUrl;
}