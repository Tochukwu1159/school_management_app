package examination.teacherAndStudents.entity;

import examination.teacherAndStudents.utils.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_templates")
@Getter
@Setter
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Auto-generated primary key

    private String code;  // Unique identifier for the template (e.g., "ATTENDANCE_ALERT")
    private String titleTemplate;  // Template string for notification title
    private String contentTemplate;  // Template string for notification body

    @Enumerated(EnumType.STRING)
    private NotificationType type;  // Enum: ALERT, REMINDER, EVENT, etc.

    private String defaultIcon;  // URL/path to default icon
    private String defaultActionUrl;  // Deep link URL
}


//{
//        "id": 1,
//        "code": "FEE_REMINDER",
//        "titleTemplate": "Fee Payment Due for ${studentName}",
//        "contentTemplate": "Dear ${parentName}, ${amount} is due by ${dueDate}",
//        "type": "REMINDER",
//        "defaultIcon": "/icons/fee-reminder.png",
//        "defaultActionUrl": "/fees/payment?studentId=${studentId}"
//        }