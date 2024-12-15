package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String firstName;
    private String lastName;

    private String email;
    private String phoneNumber;
    private String uniqueRegistrationNumber;
    private String studentGuardianName;
    private String age;
    private String classAssigned;
    private String studentGuardianPhoneNumber;
    private String gender;
    private Boolean isVerified;
    private String subjectAssigned;
    private String address;
    private String academicQualification;

    // Constructors, getters, and setters
}