package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.ContractType;
import examination.teacherAndStudents.utils.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffResponse {
    private String profilePicture;
    private String firstName;

    private String lastName;
    private String middleName;
    private String subjectAssigned;
    private String uniqueRegistrationNumber;
    private ContractType contractType;
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
}
