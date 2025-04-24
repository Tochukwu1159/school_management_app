package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.ClassLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClassLevelWithBlocksResponse {
    private Long id;
    private String className;
    private Long academicSessionId;
    private List<String> classBlocks;
}