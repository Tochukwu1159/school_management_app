package examination.teacherAndStudents.dto;

import java.time.Instant;
import java.util.Map;

public record PushNotificationDto(
        Long userId,
        String title,
        String content,
        String templateCode,
        Map<String, String> variables,
        String actionUrl,
        Instant timestamp
) {}