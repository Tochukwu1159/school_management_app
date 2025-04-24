package examination.teacherAndStudents.dto;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolResponse {
    private String schoolName;
    private String schoolAddress;
    private String schoolLogoUrl;
    private String email;
    boolean isActive;

    private String phoneNumber;

    @Column(unique = true)
    private String subscriptionKey;

    Set<Long> serviceIds; // Store selected services by their names

    private LocalDate subscriptionExpiryDate;
}
