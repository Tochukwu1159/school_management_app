package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.FeeStatus;
import examination.teacherAndStudents.utils.Purpose;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Builder
public class StudentFeeResponse {
    private Long id;
    private Long studentId;
    private Long feeId;
    private LocalDate dueDate;
    private FeeStatus status;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    private String feeName;
    private BigDecimal amount;
    private Purpose purpose;
    private boolean compulsory;
    private boolean paid;


    // Getters and setters
}