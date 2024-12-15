package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.ContractType;
import examination.teacherAndStudents.utils.Gender;
import examination.teacherAndStudents.utils.MaritalStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffRequest {
    private String profilePicture;
    private MaritalStatus maritalStatus;

    private LocalDate admissionDate;
    private String firstName;

    private String lastName;
    private String middleName;
    private String subjectAssigned;
    private String uniqueRegistrationNumber;
    private ContractType contractType;
    private Roles roles;
    private BigDecimal salary;
    private String academicQualification;
    private String bankAccountName;
    private String bankAccountNumber;
    private Gender gender;

    private String religion;

    private String address;
    private Date dateOfBirth;
    private String age;
    private String bankName;
    private String resume;
    private String phoneNumber;


// Constructors, getters, and setters
}
