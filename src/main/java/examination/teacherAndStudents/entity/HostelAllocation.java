package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.AllocationStatus;
import examination.teacherAndStudents.utils.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "hostel_allocation")
public class HostelAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hostel_id")
    private Hostel hostel; // Reference to the Hostel entity

    private int bedNumber;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Profile profile; // Reference to the student or user

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_status", nullable = false)
    private AllocationStatus allocationStatus;

    @Lob
    @Column(name = "receipt_photo")
    private byte[] receiptPhoto;

    @Column(name = "datestamp", nullable = false)
    private LocalDateTime datestamp;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @ManyToOne
    @JoinColumn(name = "due_id", nullable = false)
    private Dues dues;

    @PrePersist
    protected void onCreate() {
        this.datestamp = LocalDateTime.now();
    }


}
