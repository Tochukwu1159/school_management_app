package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.StudentTerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubjectResponse {
    private Long id;
    private String name;
}
