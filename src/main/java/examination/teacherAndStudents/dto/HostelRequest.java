package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class HostelRequest {
    private int numberOfBed;
    private String hostelType;

    private Long wardenId;

    private String hostelName;

    private String description;


}
