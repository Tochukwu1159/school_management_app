package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import examination.teacherAndStudents.utils.AvailabilityStatus;
import examination.teacherAndStudents.utils.HostelType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "hostel")
@Entity
@Builder
public class Hostel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hostel_name", nullable = false, length = 255)
    private String hostelName;

    @Enumerated(EnumType.STRING)
    private HostelType hostelType;

    @Column(name = "number_of_bed", nullable = false)
    private int numberOfBed;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private AvailabilityStatus availabilityStatus;

    @ManyToOne
    @JoinColumn(name = "warden_id")
    private Profile warden;

    @ManyToOne
    @JoinColumn(name = "warden_id2")
    private Profile warden2;

    @ManyToOne
    @JoinColumn(name = "warden_id3")
    private Profile warden3;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.availabilityStatus == null) {
            this.availabilityStatus = AvailabilityStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}