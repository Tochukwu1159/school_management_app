package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    School findBySubscriptionKey(String subscriptionKey);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsBySchoolName(String schoolName);


    Optional<School> findByEmail(String email);

    boolean existsBySchoolIdentificationNumber(String schoolIdentificationNumber);

    List<School> findByIsActiveTrueAndSubscriptionExpiryDateAfter(LocalDateTime now);

}
