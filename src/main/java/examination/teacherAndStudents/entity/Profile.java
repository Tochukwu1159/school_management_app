package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.ContractType;
import examination.teacherAndStudents.utils.Gender;
import examination.teacherAndStudents.utils.MaritalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class Profile {
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

    private String religion;

    private Boolean isVerified;
    private String address;
    private Date dateOfBirth;
    private String age;

    private String academicQualification;

    private String courseOfStudy;

    private String schoolGraduatedFrom;

    private String classOfDegree;

    @ManyToOne
    @JoinColumn(name = "classLevel_id")
    private ClassBlock classBlock;

    @ManyToOne
    @JoinColumn(name = "transport_id")
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "formclass_id")
    private ClassBlock classFormTeacher;

    @ManyToOne
    @JoinColumn(name = "subject_assigned_id")
    private Subject subjectAssigned;

    @Enumerated(value = EnumType.STRING)
    private MaritalStatus maritalStatus;

    private String bankAccountName;
    private String bankAccountNumber;
    private String bankName;
    private String resume;
    @Enumerated(value = EnumType.STRING)
    private ContractType contractType;
    private BigDecimal salary;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Wallet wallet;


    @OneToMany(mappedBy = "staff")
    private List<StaffAttendance> attendanceRecords;

    @OneToMany(mappedBy = "userProfile")
    private List<Attendance> attendances;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
