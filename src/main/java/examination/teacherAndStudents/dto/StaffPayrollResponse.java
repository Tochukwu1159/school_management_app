package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class StaffPayrollResponse {

    private Long id;
    private String name;
    private String uniqueRegistrationNumber;
    private double baseSalary;
    private double bonuses;
    private double deductions;
    private double hmo;
    private double grossPay;
    private double tax;
    private double netPay;
    private LocalDateTime datePayed;
    private String remarks;
    private Long staffId;
    private Long schoolId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
