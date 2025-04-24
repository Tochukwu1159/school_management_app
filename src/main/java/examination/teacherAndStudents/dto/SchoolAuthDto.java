package examination.teacherAndStudents.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchoolAuthDto {

    private Long id;
    private String schoolName;
    private List<String> selectedServices;
    private LocalDate subscriptionExpiryDate;
    private String schoolAddress;
    private String phoneNumber;
    private String subscriptionKey;
    private String subscriptionType;

}