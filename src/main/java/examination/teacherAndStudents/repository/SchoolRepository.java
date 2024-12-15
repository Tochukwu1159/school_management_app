package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
    School findBySubscriptionKey(String subscriptionKey);
}
