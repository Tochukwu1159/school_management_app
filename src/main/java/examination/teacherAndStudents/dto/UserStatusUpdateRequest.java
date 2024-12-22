package examination.teacherAndStudents.dto;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
public class UserStatusUpdateRequest {
    // Getter and setter
    private String action;

    private LocalDate suspensionEndDate;
}
