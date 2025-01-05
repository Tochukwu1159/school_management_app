package examination.teacherAndStudents.dto;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolResponse {
    private String schoolName;
    private String schoolAddress;
    private String schoolLogoUrl;
    private String email;

    private String phoneNumber;

    @Column(unique = true)
    private String subscriptionKey;

    @ElementCollection
    private List<String> selectedServices; // Store selected services by their names

    private LocalDate subscriptionExpiryDate;
}
