package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.Roles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notice")
@Entity
@Builder
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate eventDate;
    @Column(columnDefinition = "TEXT")
    private String eventDescription;

    @Enumerated(EnumType.STRING)
    private Roles roles;

    private LocalDate noticeDate;
    private LocalDate publishedDate;

    private LocalDate updateNoticeDate;


}
