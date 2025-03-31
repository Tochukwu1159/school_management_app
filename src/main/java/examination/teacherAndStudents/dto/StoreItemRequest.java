package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class
StoreItemRequest {
    private Long storeId;
    private String name;
    private String description;
    private String photo;
    private Double size;
    private Map<String, Integer> sizes;;
    private BigDecimal price;

    // Getters and Setters
}

