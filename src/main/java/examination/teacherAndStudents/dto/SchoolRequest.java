package examination.teacherAndStudents.dto;

import jakarta.persistence.ElementCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolRequest {
    private String schoolName;
    private String schoolAddress;

    private MultipartFile schoolImage;

    private String phoneNumber;

    @ElementCollection
    private List<String> selectedServices; // Store selected services by their names
}
