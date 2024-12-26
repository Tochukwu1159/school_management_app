package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "staff_Payroll")
@Entity
@Builder
public class StaffPayroll{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private  String uniqueRegistrationNumber;
    private double baseSalary;
    private double bonuses;
    private double deductions;
    private double tax;
    private double hmo;
    private double grossPay;
    private double netPay;

    private LocalDateTime datePayed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(name = "remarks")
    private String remarks;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

}
