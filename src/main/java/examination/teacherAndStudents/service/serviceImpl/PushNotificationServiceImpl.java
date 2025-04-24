//package examination.teacherAndStudents.service.serviceImpl;
//
//import com.google.firebase.messaging.*;
//import examination.teacherAndStudents.dto.NotificationDto;
//import examination.teacherAndStudents.entity.NotificationTemplate;
//import examination.teacherAndStudents.entity.UserDevice;
//import examination.teacherAndStudents.error_handler.EntityAlreadyExistException;
//import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
//import examination.teacherAndStudents.repository.NotificationTemplateRepository;
//import examination.teacherAndStudents.repository.UserDeviceRepository;
//import examination.teacherAndStudents.service.PushNotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PushNotificationServiceImpl implements PushNotificationService {
//
//    private final FirebaseMessaging firebaseMessaging;
//    private final UserDeviceRepository userDeviceRepository;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final NotificationTemplateRepository templateRepository;
//
//    // 1. Send to single device
//    public void sendPushNotification(Long userId, String title, String body) {
//        UserDevice device = userDeviceRepository.findByUserId(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User device not registered"));
//
//        Notification notification = Notification.builder()
//                .setTitle(title)
//                .setBody(body)
//                .build();
//
//        Message message = Message.builder()
//                .setToken(device.getFcmToken())
//                .setNotification(notification)
//                .putData("timestamp", LocalDateTime.now().toString())
//                .build();
//
//        sendFcmMessage(message, device.getFcmToken());
//    }
//
//    // 2. Send to multiple devices (broadcast)
//    public void broadcastNotification(List<Long> userIds, String title, String body) {
//        List<String> tokens = userDeviceRepository.findAllFcmTokensByUserIds(userIds);
//
//        if (!tokens.isEmpty()) {
//            MulticastMessage message = MulticastMessage.builder()
//                    .addAllTokens(tokens)
//                    .setNotification(Notification.builder()
//                            .setTitle(title)
//                            .setBody(body)
//                            .build())
//                    .build();
//
//            sendFcmMulticast(message, tokens);
//        }
//    }
//
//    // 3. Send with template
//    public void sendTemplatedNotification(Long userId, String templateCode,
//                                          Map<String, String> variables) {
//        NotificationTemplate template = templateRepository.findByCode(templateCode)
//                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
//
//        String title = replaceTemplateVariables(template.getTitleTemplate(), variables);
//        String body = replaceTemplateVariables(template.getContentTemplate(), variables);
//
//        sendPushNotification(userId, title, body);
//    }
//
//    // 4. WebSocket real-time notification
//    public void sendWebSocketNotification(Long userId, NotificationDto notification) {
//        messagingTemplate.convertAndSendToUser(
//                userId.toString(),
//                "/queue/notifications",
//                notification
//        );
//    }
//
//    // 5. Device registration
//    @Transactional
//    public void registerDevice(Long userId, String fcmToken, String deviceId) {
//        userDeviceRepository.findByDeviceId(deviceId)
//                .ifPresent(device -> {
//                    if (!device.getUserId().equals(userId)) {
//                        throw new EntityAlreadyExistException("Device already registered to another user");
//                    }
//                });
//
//        UserDevice device = userDeviceRepository.findByUserId(userId)
//                .orElse(new UserDevice());
//
//        device.setUserId(userId);
//        device.setFcmToken(fcmToken);
//        device.setDeviceId(deviceId);
//        device.setLastUpdated(LocalDateTime.now());
//
//        userDeviceRepository.save(device);
//    }
//
//    private void sendFcmMessage(Message message, String token) {
//        try {
//            String response = firebaseMessaging.send(message);
//            log.info("FCM notification sent: {}", response);
//        } catch (FirebaseMessagingException e) {
//            log.error("FCM error: {}", e.getMessage());
//            handleFcmError(e, token);
//        }
//    }
//
//    private void sendFcmMulticast(MulticastMessage message, List<String> tokens) {
//        try {
//            BatchResponse response = firebaseMessaging.sendMulticast(message);
//            log.info("FCM multicast sent: {} successes, {} failures",
//                    response.getSuccessCount(), response.getFailureCount());
//
//            if (response.getFailureCount() > 0) {
//                processFailedTokens(response, tokens);
//            }
//        } catch (FirebaseMessagingException e) {
//            log.error("FCM multicast error: {}", e.getMessage());
//        }
//    }
//
//    private void handleFcmError(FirebaseMessagingException e, String token) {
//        if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
//            log.info("Removing invalid FCM token: {}", token);
//            userDeviceRepository.deleteByFcmToken(token);
//        }
//    }
//
//    private void processFailedTokens(BatchResponse response, List<String> tokens) {
//        List<SendResponse> responses = response.getResponses();
//
//        for (int i = 0; i < responses.size(); i++) {
//            if (!responses.get(i).isSuccessful()) {
//                log.warn("Failed to send to token: {}", tokens.get(i));
//                if (responses.get(i).getException() != null &&
//                        responses.get(i).getException().getMessagingErrorCode() ==
//                                MessagingErrorCode.UNREGISTERED) {
//                    userDeviceRepository.deleteByFcmToken(tokens.get(i));
//                }
//            }
//        }
//    }
//
//    private String replaceTemplateVariables(String template, Map<String, String> variables) {
//        String result = template;
//        if (variables != null) {
//            for (Map.Entry<String, String> entry : variables.entrySet()) {
//                result = result.replace("${" + entry.getKey() + "}", entry.getValue());
//            }
//        }
//        return result;
//    }
//}
//
//
//// Send to single device
////pushNotificationService.sendPushNotification(userId, "Title", "Body");
////
////// Broadcast to multiple users
////pushNotificationService.broadcastNotification(userIds, "Title", "Body");
////
////// Send templated notification
////Map<String, String> variables = Map.of("name", "John");
////pushNotificationService.sendTemplatedNotification(userId, "WELCOME_MSG", variables);
////
////// Register device
////pushNotificationService.registerDevice(userId, "fcm-token", "device-id");