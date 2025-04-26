package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING_REVIEW;

    private String applicationNumber;

    @ElementCollection
    private Set<String> requiredDocuments;

    @ElementCollection
    private Map<String, String> submittedDocuments;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AcademicSession session;

    private boolean applicationFeeApplied = false;

    @ManyToOne
    private ClassBlock appliedClass;

    private BigDecimal applicationFee;

    private boolean feePaid = false;
    private String paymentReference;

    private LocalDateTime applicationDate;
    private LocalDateTime lastUpdated;

    private String rejectionReason;

    private boolean passed = false; // New field for exam pass status
    private int score; // New field for exam score
    private LocalDateTime examDate; // New field for scheduled exam date

    @PrePersist
    protected void onCreate() {
        applicationDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        applicationNumber = "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}