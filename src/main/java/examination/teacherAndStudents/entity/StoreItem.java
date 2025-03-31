package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "store_item")
@Entity
@Builder
public class StoreItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String photoUrl;

    @ElementCollection
    @CollectionTable(name = "store_sizes", joinColumns = @JoinColumn(name = "store_id"))
    @MapKeyColumn(name = "size")
    @Column(name = "quantity")
    private Map<String, Integer> sizes = new HashMap<>();

    private
    BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    public void updateDetails(String name, String description,
                              String photoUrl, Map<String, Integer> sizes,
                              BigDecimal price) {
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.sizes = new HashMap<>(sizes);  // Defensive copy
        this.price = price;
    }


    // Getters and Setters
}

