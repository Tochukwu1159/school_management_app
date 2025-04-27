package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class
UserResponse {
    private String firstName;
    private String lastName;
    private LocalDate admissionDate;
    private String studentGuardianOccupation;


    private String email;
    private String phoneNumber;
    private String uniqueRegistrationNumber;
    private String studentGuardianName;
    private String age;
    private String classAssigned;
    private String studentGuardianPhoneNumber;
    private String gender;
    private String subjectAssigned;
    private String address;
    private String academicQualification;

}

