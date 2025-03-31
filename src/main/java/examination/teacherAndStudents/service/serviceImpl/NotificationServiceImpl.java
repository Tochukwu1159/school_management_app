package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.TransactionRequest;
import examination.teacherAndStudents.entity.Notification;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Transaction;

import examination.teacherAndStudents.dto.NotificationResponse;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.ResourceNotFoundException;
import examination.teacherAndStudents.repository.NotificationRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.TransactionRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.NotificationService;
import examination.teacherAndStudents.utils.AccountUtils;
import examination.teacherAndStudents.utils.NotificationStatus;
import examination.teacherAndStudents.utils.NotificationType;
import examination.teacherAndStudents.utils.Roles;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;

    @Override
    public Notification studentSendMoneyNotification(TransactionRequest transactionRequest) {
        Long studentId = transactionRequest.getStudentId();
        Optional<User> student = userRepository.findById(studentId);
        if (student == null) {
            throw new CustomNotFoundException("Student with Id " + studentId + " is not valid");
        }
        Optional<Profile> profile = profileRepository.findByUser(student.get());

        Notification notification = new Notification();

        try {
            if (student == null) {
                throw new CustomNotFoundException("Id " + studentId + " is not valid");
            }

            String message = "Successfully transferred ₦" + transactionRequest.getAmount() + " to " + student.get().getFirstName();
            notification.setCreatedAt(LocalDateTime.now());
            notification.setMessage(message);
            notification.setUser(profile.get());
            notificationRepository.save(notification);
            return notification;
        } catch (CustomNotFoundException e) {
            System.out.println(e.getMessage());
        }
        notification.setMessage("Transaction failed!!!. Invalid user.");
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    @Override
    public Notification walletFundingNotification(TransactionRequest transactionRequest) {
        Long studentId = transactionRequest.getStudentId();
        Optional<User> student = userRepository.findById(studentId);
        Optional<Profile> profile = profileRepository.findByUser(student.get());

        if (student == null) {
            throw new CustomNotFoundException("Student with Id " + studentId + " is not valid");
        }

        Notification notification = new Notification();
        String message = "You have successfully funded your wallet with ₦" + transactionRequest.getAmount();
        notification.setCreatedAt(transactionRequest.getCreatedAt());
        notification.setMessage(message);
        notification.setUser(profile.get());
        notification.setNotificationStatus(NotificationStatus.UNREAD); // Set initial status to UNREAD
        return notificationRepository.save(notification);
    }



    @Override
    public List<NotificationResponse> allNotificationsOfA_StudentById() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User student = userRepository.findByEmailAndRoles(email, Roles.STUDENT)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        List<Notification> notificationEntity = notificationRepository.findNotificationByUserOrderByCreatedAtDesc(student);

        if (notificationEntity.isEmpty()) {
            throw new CustomNotFoundException("Notification is empty");
        }
        return notificationEntity.stream()
                .map(n -> new NotificationResponse(n.getMessage(), AccountUtils.localDateTimeConverter(n.getCreatedAt())))
                .toList();
    }


    @Override
    public List<NotificationResponse> allUnreadNotificationsOfAStudentById() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User student = userRepository.findByEmailAndRoles(email, Roles.STUDENT)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));
        List<Notification> unreadNotifications = notificationRepository.findNotificationByUserAndNotificationStatusOrderByCreatedAtDesc(student, NotificationStatus.UNREAD);

        if (unreadNotifications.isEmpty()) {
            throw new CustomNotFoundException("No unread notifications");
        }

        return unreadNotifications.stream()
                .map(n -> new NotificationResponse(n.getMessage(), AccountUtils.localDateTimeConverter(n.getCreatedAt())))
                .toList();
    }

    @Transactional
    public Notification createSystemNotification(Long userId, String title, String message) {
        return createSystemNotification(userId, title, message, null, null);
    }


    @Transactional
    public Notification createSystemNotification(Long userId, String title, String message,
                                                 Long transactionId, NotificationType type) {
        // Validate input
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");

        // Find user profile
        Profile userProfile = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found with ID: " + userId));

        // Resolve transaction if provided
        Transaction transaction = null;
        if (transactionId != null) {
            transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));
        }

        // Determine notification type
        NotificationType notificationType = type != null ? type : NotificationType.SYSTEM;

        // Build and save notification
        Notification notification = Notification.builder()
                .message(formatNotificationMessage(title, message))
                .notificationType(notificationType)
                .notificationStatus(NotificationStatus.UNREAD)
                .user(userProfile)
                .transaction(transaction)
                .build();

        return notificationRepository.save(notification);
    }

    private String formatNotificationMessage(String title, String message) {
        return String.format("%s: %s", title, message);
    }



    @Override
    public void markNotificationAsRead(Long notificationId) {
        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        if (notificationOptional.isEmpty()) {
            throw new CustomNotFoundException("Notification with Id " + notificationId + " not found");
        }

        Notification notification = notificationOptional.get();
        notification.setNotificationStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

}
