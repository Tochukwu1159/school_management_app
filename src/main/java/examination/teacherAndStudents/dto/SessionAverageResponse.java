package examination.teacherAndStudents.dto;
import examination.teacherAndStudents.entity.SessionAverage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionAverageResponse {
    private String studentName;
    private double averageScore;
    private String classBlock;
    private String academicSession;

    public static SessionAverageResponse fromEntity(SessionAverage sessionAverage) {
        return new SessionAverageResponse(
                sessionAverage.getUserProfile().getUser().getFirstName() + " " +sessionAverage.getUserProfile().getUser().getLastName(),
                sessionAverage.getAverageScore(),
                sessionAverage.getSessionClass().getClassBlock().getName(),
                sessionAverage.getAcademicYear().getSessionName().getName()
        );
    }
}
