package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cart") // Renamed for better clarity
@Entity
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private Profile profile; // The student adding items to the cart

    @ManyToOne
    @JoinColumn(name = "store_item_id")
    private StoreItem storeItem; // The store item being added

    private Integer quantity; // Quantity of the item in the cart

    private String size;

    private boolean isCheckedOut = false; // Status of the cart item (checked out or not)

    // Getters and Setters
}

