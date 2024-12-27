package examination.teacherAndStudents.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class StaffPayrollRequest {

    @NotNull
    private String name;

    @NotNull
    private String uniqueRegistrationNumber;

    @NotNull
    private double baseSalary;

    @NotNull
    private double bonuses;

    @NotNull
    private double deductions;

    @NotNull
    private double hmo;

    private String remarks;

    @NotNull
    private Long staffId; // ID of the staff member


    public double getGrossPay() {
        return this.baseSalary + this.bonuses - this.deductions + this.hmo;
    }

    public double getNetPay() {
        double grossPay = getGrossPay();
        double tax = grossPay * 0.05;  // Assuming 5% tax, adjust based on your requirement
        return grossPay - tax;
    }
}
