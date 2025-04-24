package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.SickLeaveStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sick_leave")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"appliedBy", "rejectedBy", "academicYear", "studentTerm"}) // Exclude relationships to avoid recursive toString
@EqualsAndHashCode(of = "id") // Only use ID for equality to avoid issues with JPA proxies
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading for better performance
    @JoinColumn(name = "applied_by_id", nullable = false)
    @NotNull(message = "Applicant profile is required")
    private Profile appliedBy;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Column(name = "days", nullable = false)
    private int days;

    @Column(name = "reason", nullable = false)
    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    @Column(name = "cancelled", nullable = false)
    private boolean cancelled = false; // Use primitive boolean with default value

    @Column(name = "cancel_reason")
    @Size(max = 255, message = "Cancel reason must not exceed 255 characters")
    private String cancelReason;

    @Column(name = "rejection_reason")
    @Size(max = 255, message = "Rejection reason must not exceed 255 characters")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status is required")
    private SickLeaveStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    @NotNull(message = "Academic year is required")
    private AcademicSession academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_id")
    private Profile rejectedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    @NotNull(message = "Student term is required")
    private StudentTerm studentTerm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}