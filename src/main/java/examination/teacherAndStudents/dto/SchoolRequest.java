package examination.teacherAndStudents.dto;

import jakarta.persistence.ElementCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
public class SchoolRequest {
    private String schoolColour;
    private String schoolName;
    private String schoolAddress;
    private String schoolLogoUrl; // URL for the school's logo
    private String phoneNumber;
    private String password;
    private String schoolMotto;
    private String alternatePhoneNumber;
    private Integer establishedYear;
    private String schoolIdentificationNumber;
    private Map<String, String> socialMediaLinks;
    private int numberOfStudents;
    private String country;
    private String state;
    private String city;
    private String email;
    private String subscriptionType; // e.g., PREMIUM, BASIC
    private String subscriptionKey;  // Key associated with the subscription
    private String subscriptionExpiryDate; // Subscription expiration date in ISO format (yyyy-MM-dd)
    private boolean isActive; // Indicates if the school is currently active
    private List<Long> selectedServices; // List of services selected by the school
}