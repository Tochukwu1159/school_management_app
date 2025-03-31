package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long id;
    private Long storeId;
    private Long profileId;
    private String itemName;
    private String itemPhoto;
    private BigDecimal itemPrice;
    private Integer quantity;
    private String size;
    private boolean isCheckedOut;

    // Getters and Setters
}

