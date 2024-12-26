package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.ClassBlock;
import examination.teacherAndStudents.entity.Subject;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    @NotNull(message = "First Name is required")
    @NotEmpty(message = "first name cannot be empty")
    private String firstName;
    private String religion;
    @NotNull(message = "Last Name is required")
    @NotEmpty(message = "Last name cannot be empty")
    private String lastName;
    private LocalDate admissionDate;
    private String studentGuardianOccupation;

    private Long schoolId;

    @NotNull(message = "Password is required")
    @NotEmpty(message = "Password cannot be empty")
    private  String password;
    @NotNull(message = "Confirm Password is required")
    @NotEmpty(message = "Confirm Password cannot be empty")
    private String confirmPassword;
    @NotNull(message = "Email is required")
    @Email
    private String email;
    @NotNull(message = "Phone Number is required")
    @NotEmpty(message = "Phone Number cannot be empty")
    private String phoneNumber;
     private String registrationNumber;
    private Date dateOfBirth;
    private Long classAssignedId;
    private  String address;
    private String age;

    private String courseOfStudy;

    private String schoolGraduatedFrom;

    private String classOfDegree;

    private String studentGuardianName;
    private String studentGuardianPhoneNumber;
    private Long subjectAssignedId;
    private String academicQualification;

    private Long classFormTeacherId;


    @NotNull(message = "Gender is required")
    @NotEmpty(message = "Gender  cannot be empty")
    private String gender;
}
