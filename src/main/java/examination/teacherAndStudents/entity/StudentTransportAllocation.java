package examination.teacherAndStudents.entity;

import java.time.LocalDateTime;

import examination.teacherAndStudents.utils.AllocationStatus;
import examination.teacherAndStudents.utils.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "student_transport_allocation")
@Builder
@Entity
public class StudentTransportAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "due_id", nullable = false)
    private Dues dues;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "transport_id")
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private AcademicSession academicSession;

    @ManyToOne
    @JoinColumn(name = "term_Id", nullable = false)
    private StudentTerm term;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationStatus status;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;


}
