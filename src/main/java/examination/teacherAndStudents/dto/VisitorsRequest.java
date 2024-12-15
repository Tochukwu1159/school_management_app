package examination.teacherAndStudents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorsRequest {
    private String name;
    private String purpose;
    private LocalDateTime signIn;
    private String phoneNumber;

    private LocalDateTime signOut;
}
