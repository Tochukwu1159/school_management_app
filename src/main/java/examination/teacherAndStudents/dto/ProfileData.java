package examination.teacherAndStudents.dto;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueRegistrationNumber() {
        return uniqueRegistrationNumber;
    }

    public void setUniqueRegistrationNumber(String uniqueRegistrationNumber) {
        this.uniqueRegistrationNumber = uniqueRegistrationNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
