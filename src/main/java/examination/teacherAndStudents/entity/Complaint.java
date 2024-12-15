package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "complaint")

@Builder
public class Complaint{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String feedbackText;
    private LocalDateTime submittedTime;
    private String replyText;
    private LocalDateTime replyTime;
    private String reply;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
