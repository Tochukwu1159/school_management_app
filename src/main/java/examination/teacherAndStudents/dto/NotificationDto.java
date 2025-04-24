package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.utils.NotificationType;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String title,
        String content,
        NotificationType type,
        LocalDateTime createdAt,
        boolean isRead,
        String actionUrl,
        String icon
) {}
