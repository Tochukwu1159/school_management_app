package examination.teacherAndStudents.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ProfileData {
    private Long id;
    private String uniqueRegistrationNumber;
    private String phoneNumber;

    public ProfileData(Long id, String uniqueRegistrationNumber, String phoneNumber) {
        this.id = id;
        this.uniqueRegistrationNumber = uniqueRegistrationNumber;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters

}
