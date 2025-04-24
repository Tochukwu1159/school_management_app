package examination.teacherAndStudents.dto;

import lombok.Data;

import java.util.Set;

@Data
public class StoreRequest {
    private String name;
    private Set<Long> categoryIds;
}