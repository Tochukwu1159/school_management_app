package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.StoreItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreItemResponse {
    private Long id;
    private String name;
    private String description;
    private String photo;
    private String photoUrl;
    private Long storeId;
    private Map<String, Integer> sizes;
    private BigDecimal price;

    public static StoreItemResponse from(StoreItem item) {
        return StoreItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .photoUrl(item.getPhotoUrl())
//                .sizes(new HashMap<>(item.getSizes()))
                .price(item.getPrice())
                .storeId(item.getStore().getId())
                .build();
    }

    // Getters and Setters
}

