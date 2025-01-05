package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class StoreRequest {
    private String name;
    private String description;
    private String photo;
    private Double size;
    private Map<Integer, Integer> sizes;;
    private Double price;

    // Getters and Setters
}

