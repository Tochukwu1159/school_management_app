package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import examination.teacherAndStudents.utils.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
//@Table(name = "profile")
public class Profile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

//    @Column(nullable = false)
    private LocalDate admissionDate;

    @Column(unique = true, nullable = false)
    private String uniqueRegistrationNumber;

    private String studentGuardianName;

    private String studentGuardianOccupation;

    private String profilePicture;

    private String studentGuardianPhoneNumber;

    @Column(nullable = false)
    private String gender;

    private String bankAccountId;

    private String religion;

    @Column(nullable = false)
    private Boolean isVerified;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id") // Changed from address_id
    private Set<Address> addresses = new HashSet<>();

    @OneToOne(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private BankDetails bankDetails;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    private Set<EmergencyContact> emergencyContacts = new HashSet<>();

//    @Column(nullable = false)
    private LocalDate dateOfBirth; // Changed from Date

    private String referralCode;

    private String referralLink;

    private String academicQualification;

    private String courseOfStudy;

    private String schoolGraduatedFrom;

    private String classOfDegree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_level_id")
    private ClassBlock classBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_class_id")
    private ClassBlock classFormTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_assigned_id")
    private Subject subjectAssigned;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaritalStatus maritalStatus;

    private String resumeUrl;

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_id")
    private Bus transport;

    @Column(nullable = false)
    private int annualLeaveDays = 30;

    @Column(nullable = false)
    private int remainingLeaveDays = 30;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_level_id")
    private StaffLevel staffLevel;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<StaffAttendance> attendanceRecords;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Attendance> attendances;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus profileStatus = ProfileStatus.ACTIVE;

    private LocalDate suspensionEndDate;

}