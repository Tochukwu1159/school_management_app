package examination.teacherAndStudents.entity;
import examination.teacherAndStudents.utils.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "hostel_bed_tracker")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostelBedTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @OneToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @Column(name = "beds_allocated", nullable = false)
    private int bedsAllocated;

    @PrePersist
    protected void onCreate() {
        this.bedsAllocated = 0;
    }
}
