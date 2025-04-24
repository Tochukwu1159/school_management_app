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
    private String name;
    private Long classLevelId;
    private Long formTeacherId;
    private String classUniqueUrl;
    private String classLevelName;
    private String formTeacherName;
    private int numberOfStudents;
}
