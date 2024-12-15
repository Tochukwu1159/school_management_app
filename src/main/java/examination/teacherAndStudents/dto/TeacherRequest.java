package examination.teacherAndStudents.dto;
import examination.teacherAndStudents.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherRequest {
        private String address;
        private String phoneNumber;
        private String firstName;
        private String registrationNumber;
        private String dateOfBirth;
        private  String lastName;
        private String email;
        private String formTeacher;
        private String gender;
        private String academicQualification;
        private Subject subjectAssigned;
        private String religion;
        private Year year;

        private String password;
        private String confirmPassword;


}
