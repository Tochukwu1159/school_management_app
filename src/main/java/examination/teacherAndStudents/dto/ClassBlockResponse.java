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
public class ClassBlockResponse {
    private Long id;
    private String currentStudentClassName;
    private Long classLevelId;
    private Long formTeacherId;
    private String classUniqueUrl;
    private int numberOfStudents;
    private StudentTerm term;
}
