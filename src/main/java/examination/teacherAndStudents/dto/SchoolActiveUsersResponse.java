package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolActiveUsersResponse {
    private int numOfStudents;
    private int numOfStaff;
    private BigDecimal percentOfBoys;
    private BigDecimal percentOfGirls;
    private BigDecimal percentOfMaleStaff;
    private BigDecimal percentOfFemaleStaff;
}