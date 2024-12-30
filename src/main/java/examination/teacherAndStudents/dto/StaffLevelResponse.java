package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StaffLevelResponse {
    private Long id;
    private String name;
    private double grossSalary;
    private double baseSalary;
    private double hmo;
    private double netSalary;
    private double tax;
}
