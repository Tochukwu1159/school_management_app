package examination.teacherAndStudents.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class StoreItemRequest {
    @NotNull
    private String name;
    @NotNull
    private Long storeId;
    @NotNull
    private Long categoryId;
    @NotNull
    private BigDecimal price;
    private String description;
    private String photo;
    private Map<String, Integer> sizes;
    private Integer quantity;

    // Custom validation to ensure at least one of sizes or quantity is provided
    public void validate() {
        if ((sizes == null || sizes.isEmpty()) && quantity == null) {
            throw new IllegalArgumentException("Either sizes or quantity must be provided");
        }
        if (sizes != null && !sizes.isEmpty() && quantity != null) {
            throw new IllegalArgumentException("Cannot provide both sizes and quantity");
        }
    }
}