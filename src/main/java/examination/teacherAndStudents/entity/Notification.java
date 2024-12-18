package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import examination.teacherAndStudents.utils.NotificationStatus;
import examination.teacherAndStudents.utils.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
//@Table(
//        name = "notification",
//        indexes = {
//                @Index(name = "idx_notification_user_id", columnList = "user_id"),
//                @Index(name = "idx_notification_type", columnList = "notification_type"),
//                @Index(name = "idx_notification_status", columnList = "notification_status"),
//                @Index(name = "idx_notification_created_at", columnList = "createdAt")
//        }
//)
@Table(
        name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "message",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus; // Fixed typo

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Profile user;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    // Optionally, you could add an updated timestamp or status flag if needed:
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
