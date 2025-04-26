package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.MembershipStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "library_membership")
public class LibraryMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Profile student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status; // ACTIVE, SUSPENDED, EXPIRED

    @NotNull
    @Column(name = "join_date", nullable = false, updatable = false)
    private LocalDateTime joinDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.joinDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = MembershipStatus.ACTIVE;
        }
        if (this.expiryDate == null) {
            this.expiryDate = this.joinDate.plusYears(1); // Default 1-year membership
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}