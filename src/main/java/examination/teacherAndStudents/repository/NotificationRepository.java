package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Notification;
import examination.teacherAndStudents.entity.Transaction;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findNotificationByUserOrderByCreatedAtDesc(User user);

    List<Notification> findNotificationByUserAndNotificationStatusOrderByCreatedAtDesc(User student, NotificationStatus unread);
}
