// ENTITY
package examination.teacherAndStudents.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class StaffMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Profile staff;

    @Column(nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "verified_id", nullable = false)
    private Profile verifiedBy;



    public enum Status {
        PENDING,
        APPROVED,
        RETURNED,
        VERIFIED,
        REJECTED
    }

    @ManyToOne
    @JoinColumn(name = "approvedBy_id", nullable = false)
    private Profile approvedBy;

    @Column(nullable = false)
    private LocalDateTime expectedReturnTime;

    private LocalDateTime actualReturnTime;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
