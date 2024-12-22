package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.Roles;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NoticeRequest {
    private String title;
    @Column(columnDefinition = "TEXT")
    private String eventDescription;
}
