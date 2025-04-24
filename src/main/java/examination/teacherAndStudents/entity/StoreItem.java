package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "store_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"school", "store", "category"})
@EqualsAndHashCode(of = "id")
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "photo_url")
    private String photoUrl;

    @ElementCollection
    @CollectionTable(name = "store_sizes", joinColumns = @JoinColumn(name = "store_item_id"))
    @MapKeyColumn(name = "size")
    @Column(name = "quantity")
    private Map<String, Integer> sizes;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    private Category category;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public void updateDetails(String name, String description, String photoUrl, Map<String, Integer> sizes, Integer quantity, BigDecimal price, Category category) {
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.sizes = sizes != null ? new HashMap<>(sizes) : null; // Defensive copy
        this.quantity = quantity;
        this.price = price;
        this.category = category;
    }

    public void reduceStock(String size, Integer qty) {
        if (sizes != null && size != null) {
            Integer currentQty = sizes.get(size);
            if (currentQty == null || currentQty < qty) {
                throw new IllegalArgumentException("Insufficient stock for size: " + size);
            }
            sizes.put(size, currentQty - qty);
            if (sizes.get(size) == 0) {
                sizes.remove(size);
            }
        } else if (quantity != null) {
            if (quantity < qty) {
                throw new IllegalArgumentException("Insufficient stock for item: " + name);
            }
            quantity -= qty;
        } else {
            throw new IllegalStateException("Item has no stock information");
        }
    }
}