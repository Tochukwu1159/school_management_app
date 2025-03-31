package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "staff_attendance")
public class StaffAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_unique_reg_number", nullable = false)
    private String staffUniqueRegNumber;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "check_in_thumbprint_hash")
    private String checkInThumbprintHash;

    @Column(name = "check_out_thumbprint_hash")
    private String checkOutThumbprintHash;

    @Column(name = "biometric_device_id")
    private String biometricDeviceId;
    @Column(name = "checkOut_biometric_deviceId")
    private String checkOutBiometricDeviceId;

    @Column(name = "check_out_verification_score")
    private double checkOutVerificationScore;

    @Column(name = "verification_score")
    private Double verificationScore;

    private String thumbprintHash;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private StudentTerm studentTerm;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private Profile staff;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Additional biometric metadata
    @Column(name = "biometric_match_algorithm")
    private String biometricMatchAlgorithm;

    @Column(name = "biometric_sdk_version")
    private String biometricSdkVersion;
}