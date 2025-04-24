package examination.teacherAndStudents.service;

import java.util.List;
import java.util.Map;

public interface PushNotificationService {
    void sendPushNotification(Long userId, String title, String body);
    void broadcastNotification(List<Long> userIds, String title, String body);
    void sendTemplatedNotification(Long userId, String templateCode,
                                   Map<String, String> variables);
    void registerDevice(Long userId, String fcmToken, String deviceId);
}
