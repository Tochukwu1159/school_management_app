package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StaffLevelResponse {
    private Long id;
    private String name;
    private double grossSalary;
    private double baseSalary;
    private double hmo;
    private double netSalary;
    private double tax;
}
