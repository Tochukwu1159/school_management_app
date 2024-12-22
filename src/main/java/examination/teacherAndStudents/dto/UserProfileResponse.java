package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String uniqueRegistrationNumber;
    private String phoneNumber;
    private Roles roles;
}