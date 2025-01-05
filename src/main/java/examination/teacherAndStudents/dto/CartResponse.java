package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long id;
    private Long storeId;
    private Long profileId;
    private String itemName;
    private String itemPhoto;
    private Double itemPrice;
    private Integer quantity;
    private String size;
    private boolean isCheckedOut;

    // Getters and Setters
}

