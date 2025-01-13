package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import examination.teacherAndStudents.utils.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Profile")
@Entity
@Builder
public class  Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String phoneNumber;
    private LocalDate admissionDate;
    private String uniqueRegistrationNumber;
    private String studentGuardianName;
    private String studentGuardianOccupation;
    private String profilePicture;
    //    private String classAssigned;
    private String studentGuardianPhoneNumber;
    private String gender;

    private String bankAccountId;

    private String religion;

    private Boolean isVerified;


    private String address;

    private String country;

    private String state;

    private String city;

    private Date dateOfBirth;

    private String academicQualification;

    private String courseOfStudy;

    private String schoolGraduatedFrom;

    private String classOfDegree;

    @ManyToOne
    @JoinColumn(name = "classLevel_id")
    @ToString.Exclude
    private ClassBlock classBlock;

    @ManyToOne
    @JoinColumn(name = "formclass_id")
    @ToString.Exclude
    private ClassBlock classFormTeacher;

    @ManyToOne
    @JoinColumn(name = "subject_assigned_id")
    @ToString.Exclude
    private Subject subjectAssigned;

    @Enumerated(value = EnumType.STRING)
    private MaritalStatus maritalStatus;

    private String bankAccountName;
    private String bankAccountNumber;
    private String bankName;
    private String resume;
    @Enumerated(value = EnumType.STRING)
    private ContractType contractType;

    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @OneToOne(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @ToString.Exclude
    private Wallet wallet;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "transport_id")
    private Transport transport;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "staff_level_id")
    private StaffLevel staffLevel;

    @OneToMany(mappedBy = "staff")
    @ToString.Exclude
    @JsonBackReference
    private List<StaffAttendance> attendanceRecords;

    @OneToMany(mappedBy = "userProfile")
    @ToString.Exclude
    @JsonBackReference
    private List<Attendance> attendances;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private ProfileStatus profileStatus = ProfileStatus.ACTIVE;  // Default status

    private LocalDate suspensionEndDate;  // Date when the suspension ends

    public boolean isSuspended() {
        return ProfileStatus.SUSPENDED.equals(profileStatus) && suspensionEndDate != null && suspensionEndDate.isAfter(LocalDate.now());
    }


    @PrePersist
    @PreUpdate
    public void checkSuspensionStatus() {
        // If the profile is suspended and the current date is after the suspension end date,
        // update the status to ACTIVE.
        if (profileStatus == ProfileStatus.SUSPENDED && suspensionEndDate != null) {
            if (suspensionEndDate.isBefore(LocalDate.now())) {
                profileStatus = ProfileStatus.ACTIVE;
                suspensionEndDate = null; // Optional: Clear the suspension end date after status update.
            }
        }
    }
}
