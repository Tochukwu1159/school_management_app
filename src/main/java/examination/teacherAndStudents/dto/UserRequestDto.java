package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.ContractType;
import examination.teacherAndStudents.utils.MaritalStatus;
import examination.teacherAndStudents.utils.Roles;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    // Personal Information
    @NotNull(message = "First Name is required")
    @NotEmpty(message = "First name cannot be empty")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotNull(message = "Last Name is required")
    @NotEmpty(message = "Last name cannot be empty")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotNull(message = "Middle Name is required")
    @NotEmpty(message = "Middle name cannot be empty")
    @Size(max = 50, message = "Middle name cannot exceed 50 characters")
    private String middleName;

    @NotNull(message = "Gender is required")
    @NotEmpty(message = "Gender cannot be empty")
    private String gender;

//    @NotNull(message = "Date of Birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Marital Status is required")
    private MaritalStatus maritalStatus;

    // Contact Information
    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotNull(message = "Phone Number is required")
    @NotEmpty(message = "Phone Number cannot be empty")
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Phone number must be between 8-15 digits")
    private String phoneNumber;

    private Set<AddressDto> addresses;
//    @NotNull(message = "Emergency contacts cannot be null")
    private List<EmergencyContactDto> emergencyContacts;



    // Institution Information
//    @NotNull(message = "School ID is required")
    private Long schoolId;

    private String referralCode;


    private Long classAssignedId;
    private Long subjectAssignedId;
    private Long classFormTeacherId;
    private Long staffLevelId;

    // Education Information
    private String courseOfStudy;
    private String schoolGraduatedFrom;
    private String classOfDegree;
    private String academicQualification;

    // Guardian Information
    private String studentGuardianName;
    private String studentGuardianPhoneNumber;
    private String studentGuardianOccupation;

    // Additional Fields
    private String religion;
    private String age;
    private String registrationNumber;
    private LocalDate admissionDate;
    private ContractType contractType;
    private BigDecimal salary;
    private Roles role;

    // Documents
//    @NotNull(message = "At least one document is required")
//    @Size(min = 1, message = "At least one document is required")
    private List<DocumentDto> documents;

    // Profile Picture (optional)
    private MultipartFile profilePicture;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentDto {
        @NotNull(message = "Document type is required")
        private String documentType;

        @NotNull(message = "Document file is required")
        private MultipartFile file;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressDto {
        private Long id;
        private String street;
        private String postalCode;
        private boolean isPrimary;

        // Location Information
        @NotNull(message = "Country is required")
        @NotEmpty(message = "Country cannot be empty")
        private String country;

        @NotNull(message = "State is required")
        @NotEmpty(message = "State cannot be empty")
        private String state;

        @NotNull(message = "City is required")
        @NotEmpty(message = "City cannot be empty")
        private String city;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static
    class  EmergencyContactDto {
        private String name;
        private String phone;
        private String relationship;
    }

}