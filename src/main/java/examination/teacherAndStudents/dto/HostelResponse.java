package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostelResponse {
    private Long id;
    private String hostelName;
    private Integer numberOfBed;
    private BigDecimal costPerBed;
    private AvailabilityStatus availabilityStatus;
    private Long schoolId;
    private String schoolName;
    private Long wardenId;
}