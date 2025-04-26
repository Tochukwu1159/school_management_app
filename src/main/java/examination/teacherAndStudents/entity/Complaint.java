package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.ComplainStatus;
import examination.teacherAndStudents.utils.StudentTerm;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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

    private String photo;

    private LocalDateTime replyTime;

    @ManyToOne
    @JoinColumn(name = "complained_user_id")
    private Profile complainedBy;

    @Enumerated(EnumType.STRING)
    private ComplainStatus complainStatus;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    @ManyToOne
    @JoinColumn(name = "replied_user_id")
    private Profile repliedBy;
    @PrePersist
    private void onCreate() {
        if (submittedTime == null) {
            submittedTime = LocalDateTime.now();
        }
    }
    public void setReplyText(String replyText) {
        this.replyText = replyText;
        if (replyText != null && !replyText.isEmpty()) {
            this.replyTime = LocalDateTime.now();
        }
    }
}

