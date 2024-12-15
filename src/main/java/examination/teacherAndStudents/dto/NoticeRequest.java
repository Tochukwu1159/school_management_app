package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.Roles;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NoticeRequest {
    private String title;
    private LocalDate noticeDate;
    private LocalDate publishedDate;

    private Roles role;
    @Column(columnDefinition = "TEXT")
    private String eventDescription;
}
