package examination.teacherAndStudents.service;
import examination.teacherAndStudents.dto.NotificationResponse;
import examination.teacherAndStudents.dto.TransactionRequest;
import examination.teacherAndStudents.entity.Notification;
import examination.teacherAndStudents.utils.NotificationType;

import java.util.List;

public interface NotificationService {
    Notification studentSendMoneyNotification(TransactionRequest transactionRequest);

    Notification walletFundingNotification(TransactionRequest transactionRequest);


    List<NotificationResponse> allNotificationsOfA_StudentById();
    void markNotificationAsRead(Long notificationId);
    List<NotificationResponse> allUnreadNotificationsOfAStudentById();

    Notification createSystemNotification(Long userId, String title, String message,
                                          Long transactionId, NotificationType type);
    Notification createSystemNotification(Long userId, String title, String message);
}


