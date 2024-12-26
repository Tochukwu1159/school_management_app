package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Subject;
import examination.teacherAndStudents.entity.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
}
