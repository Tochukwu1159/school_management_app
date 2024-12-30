package examination.teacherAndStudents.dto;

import lombok.Data;

@Data
public class StaffLevelRequest {
    private String name;
    private double grossSalary;
    private double baseSalary;
    private double hmo;
    private double netSalary;
    private double tax;
}
