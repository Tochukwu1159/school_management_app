package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartRequest {
    @NotNull
    private Long storeItemId;
    private String size;
    @NotNull
    private Integer quantity;
}