package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.VisitorStatus;
import examination.teacherAndStudents.utils.VisitorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorsResponse {
    private Long id;
    private String name;
    private String purpose;
    private String phoneNumber;
    private String hostName;
    private String email;
    private VisitorType visitorType;
    private VisitorStatus status;
    private LocalDateTime signIn;
    private LocalDateTime signOut;
}