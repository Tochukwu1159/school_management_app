package examination.teacherAndStudents.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "maintenance")
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private double amountSpent;

    @OneToOne
    @JoinColumn(name = "transport_id", nullable = false)
    private Bus transport;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile maintainedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime maintenanceDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        maintenanceDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}