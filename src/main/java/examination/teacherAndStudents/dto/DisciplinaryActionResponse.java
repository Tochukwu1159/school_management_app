package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.DisciplinaryActionType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DisciplinaryActionResponse {
    private Long id;
    private String regNo;
    private Long profileId;
    private String profileName;
    private Long issuedById;
    private String issuedByName;
    private DisciplinaryActionType actionType;
    private String reason;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
}
