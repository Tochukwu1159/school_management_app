package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hostel_bed_tracker")
@Getter
@Setter
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

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "beds_allocated", nullable = false)
    private int bedsAllocated;

    @Column(name = "number_of_bed_left", nullable = false)
    private int numberOfBedLeft;

    @PrePersist
    protected void onCreate() {
        this.bedsAllocated = 0;
        this.numberOfBedLeft = hostel != null ? hostel.getNumberOfBed() : 0;
    }

    public void allocateBed() {
        if (numberOfBedLeft <= 0) {
            throw new IllegalStateException("No beds available in hostel");
        }
        bedsAllocated++;
        numberOfBedLeft--;
    }

    public void deallocateBed() {
        if (bedsAllocated <= 0) {
            throw new IllegalStateException("No beds allocated to deallocate");
        }
        bedsAllocated--;
        numberOfBedLeft++;
    }
}