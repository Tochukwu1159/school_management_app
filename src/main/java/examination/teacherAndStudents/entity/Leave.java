package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.SickLeaveStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "leave")
@Entity
@Builder
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "applied_by_id", nullable = false)
    private Profile appliedBy;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    private Boolean cancelled;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "reason", nullable = false)
    @NotNull
    @Size(max = 255)
    private String reason;

    @Size(max = 255)
    @Column(name = "cancel_reason")
    private String cancelReason;

    @Size(max = 255)
    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)

    @Column(name = "status", nullable = false)
    private SickLeaveStatus status;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicSession academicYear;

    @ManyToOne
    @JoinColumn(name = "rejected_by_id")
    private Profile rejectedBy;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private StudentTerm studentTerm;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

