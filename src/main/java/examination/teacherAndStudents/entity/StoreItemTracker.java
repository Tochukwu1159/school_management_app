package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
        import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "store_item_tracker")
public class StoreItemTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_item_id", nullable = false)
    private StoreItem storeItem;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @Column(name = "store_item_remaining", nullable = false)
    private int storeItemRemaining;

    @PrePersist
    protected void onCreate() {
        this.storeItemRemaining = 0;
    }

}
