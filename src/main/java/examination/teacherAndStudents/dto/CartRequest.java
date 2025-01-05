package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartRequest {
    private Long storeItemId;
    private Integer quantity;
    private String size;

    // Getters and Setters
}
